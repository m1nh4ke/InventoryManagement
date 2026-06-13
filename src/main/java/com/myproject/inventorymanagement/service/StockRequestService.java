package com.myproject.inventorymanagement.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.myproject.inventorymanagement.dto.CreateStockRequestDto;
import com.myproject.inventorymanagement.entity.Product;
import com.myproject.inventorymanagement.entity.StockMovement;
import com.myproject.inventorymanagement.entity.StockRequest;
import com.myproject.inventorymanagement.entity.StockRequestItem;
import com.myproject.inventorymanagement.entity.User;
import com.myproject.inventorymanagement.repository.ProductRepository;
import com.myproject.inventorymanagement.repository.StockMovementRepository;
import com.myproject.inventorymanagement.repository.StockRequestItemRepository;
import com.myproject.inventorymanagement.repository.StockRequestRepository;
import com.myproject.inventorymanagement.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockRequestService {

    private final StockRequestRepository stockRequestRepository;
    private final StockRequestItemRepository stockRequestItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InvoiceService invoiceService;

    public List<StockRequest> getAllRequests() {
        return stockRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<StockRequest> getRequestsByStaff(String username) {
        User staff = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Staff user not found: " + username));
        return stockRequestRepository.findByStaffOrderByCreatedAtDesc(staff);
    }

    public StockRequest getRequestById(Long id) {
        return stockRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock request not found with id: " + id));
    }

    @Transactional
    public StockRequest createRequest(CreateStockRequestDto dto, String username) {
        User staff = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        StockRequest request = new StockRequest();
        request.setType(StockRequest.RequestType.valueOf(dto.getType().toUpperCase()));
        request.setStatus(StockRequest.RequestStatus.PENDING);
        request.setStaff(staff);
        request.setReason(dto.getReason());

        StockRequest savedRequest = stockRequestRepository.save(request);

        if (dto.getItems() != null) {
            for (CreateStockRequestDto.ItemDto itemDto : dto.getItems()) {
                Product product = productRepository.findById(itemDto.getProductId())
                        .orElseThrow(() -> new RuntimeException(
                                "Không tìm thấy sản phẩm với id: " + itemDto.getProductId()));

                if (itemDto.getQuantity() <= 0) {
                    throw new RuntimeException("Số lượng phải lớn hơn 0!");
                }

                if (request.getType() == StockRequest.RequestType.EXPORT
                        && product.getQuantity() < itemDto.getQuantity()) {
                    throw new RuntimeException("Trong kho không đủ sản phẩm: " + product.getName() + ". Còn tồn: "
                            + product.getQuantity() + ", Yêu cầu: " + itemDto.getQuantity());
                }

                StockRequestItem item = new StockRequestItem();
                item.setRequest(savedRequest);
                item.setProduct(product);
                item.setQuantity(itemDto.getQuantity());
                item.setSystemQuantity(product.getQuantity());
                item.setActualQuantity(itemDto.getQuantity());

                stockRequestItemRepository.save(item);
                savedRequest.getItems().add(item);
            }
        }

        return savedRequest;
    }

    @Transactional
    public StockRequest approveRequest(Long id, String managerUsername) {
        User manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Manager not found: " + managerUsername));

        StockRequest request = getRequestById(id);
        if (request.getStatus() != StockRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        for (StockRequestItem item : request.getItems()) {
            Product product = item.getProduct();
            if (request.getType() == StockRequest.RequestType.IMPORT) {
                product.setQuantity(product.getQuantity() + item.getQuantity());
                productRepository.save(product);

                StockMovement movement = new StockMovement();
                movement.setProduct(product);
                movement.setUser(manager);
                movement.setType(StockMovement.MovementType.IN);
                movement.setQuantity(item.getQuantity());
                movement.setReason("Yêu cầu #" + request.getId()
                        + (request.getReason() != null && !request.getReason().trim().isEmpty() ? " - " + request.getReason() : ""));
                movement.setReferenceId(request.getId());
                stockMovementRepository.save(movement);
            } else if (request.getType() == StockRequest.RequestType.EXPORT) {
                if (product.getQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Not enough stock for product: " + product.getName() + ". Available: "
                            + product.getQuantity());
                }
                product.setQuantity(product.getQuantity() - item.getQuantity());
                productRepository.save(product);

                StockMovement movement = new StockMovement();
                movement.setProduct(product);
                movement.setUser(manager);
                movement.setType(StockMovement.MovementType.OUT);
                movement.setQuantity(item.getQuantity());
                movement.setReason("Yêu cầu #" + request.getId()
                        + (request.getReason() != null && !request.getReason().trim().isEmpty() ? " - " + request.getReason() : ""));
                movement.setReferenceId(request.getId());
                stockMovementRepository.save(movement);
            }
        }

        request.setStatus(StockRequest.RequestStatus.APPROVED);
        request.setManager(manager);
        request.setUpdatedAt(LocalDateTime.now());
        StockRequest approvedRequest = stockRequestRepository.save(request);

        // Generate Invoice
        invoiceService.createInvoiceFromRequest(approvedRequest, manager);

        return approvedRequest;
    }

    @Transactional
    public StockRequest rejectRequest(Long id, String managerUsername) {
        User manager = userRepository.findByUsername(managerUsername)
                .orElseThrow(() -> new RuntimeException("Manager not found: " + managerUsername));

        StockRequest request = getRequestById(id);
        if (request.getStatus() != StockRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request is not in PENDING status");
        }

        request.setStatus(StockRequest.RequestStatus.REJECTED);
        request.setManager(manager);
        request.setUpdatedAt(LocalDateTime.now());
        return stockRequestRepository.save(request);
    }
}
