package org.example.voucher.controller;

import com.google.gson.Gson;
import org.example.common.dto.OtpRequest;
import org.example.common.dto.OtpResponse;
import org.example.common.dto.OtpValidation;
import org.example.common.dto.OtpValidationResponse;
import org.example.voucher.dto.ListVoucherRequestDto;
import org.example.voucher.service.OtpServiceProxy;
import org.example.voucher.service.VoucherService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
//@SpringBootTest
@WebMvcTest(controllers = VoucherController.class)
public class VoucherControllerUnitTest {

    @Autowired
    private VoucherController voucherController;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    private OtpServiceProxy otpServiceProxy;

    @MockBean
    private VoucherService voucherService;

    @Test
    public void testListVoucherRequestOtp() throws Exception {
        ListVoucherRequestDto requestDto = new ListVoucherRequestDto("07777777", null);
        String json = new Gson().toJson(requestDto);
        when(otpServiceProxy.requestOtp(any(OtpRequest.class))).thenReturn(new OtpResponse("referenceNo"));
        mockMvc.perform(post("/list-voucher").contentType(MediaType.APPLICATION_JSON_VALUE).content(json))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.referenceNo").exists())
                .andDo(mvcResult -> System.out.println(mvcResult.getResponse().getContentAsString()));
    }

    @Test
    public void testListVoucherReturn() throws Exception {
        ListVoucherRequestDto requestDto = new ListVoucherRequestDto("07777777",
                new OtpValidation("123456", "refNo", "07777777"));
        String json = new Gson().toJson(requestDto);
        when(otpServiceProxy.validateOtp(any(OtpValidation.class)))
                .thenReturn(new OtpValidationResponse("refNo", true));
        when(voucherService.getPurchasedVoucher("07777777"))
                .thenReturn(Arrays.asList("voucher-code1", "voucher-code2"));
        mockMvc.perform(post("/list-voucher").contentType(MediaType.APPLICATION_JSON_VALUE).content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andDo(mvcResult -> System.out.println(mvcResult.getResponse().getContentAsString()));
    }
}
