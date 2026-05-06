package ke.co.legalbridge.authservice.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import ke.co.legalbridge.authservice.dto.DeleteAccountRequestDTO;
import ke.co.legalbridge.authservice.service.DeleteAccountService;
import ke.co.legalbridge.authservice.apiresponse.ApiResponse;
import ke.co.legalbridge.authservice.apiresponse.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
@Tag(name = "Delete Account Service", description = "Deactivates accounts on delete requests.")
public class DeleteAccountController {

    private final DeleteAccountService deleteAccountService;

    @PatchMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(@Valid @RequestBody DeleteAccountRequestDTO requestDTO) {
        return ResponseEntityBuilder.ok(deleteAccountService.deactivateAccount(requestDTO)).build();
    }

}
