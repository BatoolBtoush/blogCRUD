package com.batool.crud.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('ROLE_NORMAL', 'ROLE_ADMIN')")
public class CRUDController {

    @PostMapping("/hey")
    public String hey(){
        return "hey";
    }
}
