package com.example.controller;

import com.example.facade.PdfResearchFacade;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@Slf4j
@RestController
@RequestMapping("pdf")
@AllArgsConstructor
public class PdfResearchController {

    private final PdfResearchFacade pdfResearchFacade;

    @GetMapping
    public void pdf(@RequestParam String fontName, HttpServletResponse response){
        pdfResearchFacade.pdfResearch(fontName, response);
    }


}
