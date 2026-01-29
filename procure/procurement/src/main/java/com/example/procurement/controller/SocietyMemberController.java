package com.example.procurement.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.procurement.DTO.SocietyMemberRequest;
import com.example.procurement.entity.SocietyUser;
import com.example.procurement.service.SocietyMemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class SocietyMemberController {

    private final SocietyMemberService memberService;

    @PostMapping("/onboard")
    public ResponseEntity<SocietyUser> onboardMember(@RequestBody SocietyMemberRequest dto) {
        return ResponseEntity.ok(memberService.onboard(dto));
    }

    @GetMapping
    public List<SocietyUser> getAllMembers() {
        return memberService.findAll();
    }
}

