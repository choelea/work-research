package com.example.component;

import com.itextpdf.text.Font;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * @author : Joe
 * @date : 2022/8/10
 */
@Slf4j
public class MyFontProvider extends XMLWorkerFontProvider {
    private String myFont;

    public MyFontProvider(String myFont) {
        super();
        this.myFont = myFont;
        log.info(("----------------------------------------Font Family Started-----------------------------------"));
        this.getRegisteredFamilies().forEach(key->{
            log.info(key);
        });
        log.info(("----------------------------------------Font Family Ended-----------------------------------"));
    }

    public Font getFont(String fontName, String encoding, float size, final int style) {
        fontName = myFont;
        return super.getFont(fontName, encoding, size, style);
    }
}
