package ke.co.legalbridge.Auth_Service.controller;

import jakarta.validation.Valid;
import ke.co.legalbridge.Auth_Service.dto.DeleteAccountRequestDTO;
import ke.co.legalbridge.Auth_Service.service.DeleteAccountService;
import ke.co.legalbridge.sharedlibraries.response.ApiResponse;
import ke.co.legalbridge.sharedlibraries.response.ResponseEntityBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class DeleteAccountController {

    private final DeleteAccountService deleteAccountService;

    @PatchMapping("/deactivate")
    public ResponseEntity<ApiResponse<String>> deactivateAccount(@Valid @RequestBody DeleteAccountRequestDTO requestDTO) {
        return ResponseEntityBuilder.ok(deleteAccountService.deactivateAccount(requestDTO)).build();
    }

}
