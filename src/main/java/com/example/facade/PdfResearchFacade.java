package com.example.facade;

import com.example.component.MyFontProvider;
import com.example.config.FreemarkerConfiguration;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.Pipeline;
import com.itextpdf.tool.xml.XMLWorker;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.html.CssAppliersImpl;
import com.itextpdf.tool.xml.html.Tags;
import com.itextpdf.tool.xml.net.FileRetrieve;
import com.itextpdf.tool.xml.net.ReadingProcessor;
import com.itextpdf.tool.xml.parser.XMLParser;
import com.itextpdf.tool.xml.pipeline.css.CSSResolver;
import com.itextpdf.tool.xml.pipeline.css.CssResolverPipeline;
import com.itextpdf.tool.xml.pipeline.end.PdfWriterPipeline;
import com.itextpdf.tool.xml.pipeline.html.*;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PdfResearchFacade {
    private static final String CHARSET_NAME = "UTF-8";
    private static final String FONT = "STSongStd-Light";
    @SneakyThrows
    public void pdfResearch(String fontNameParam, HttpServletResponse response){
        response.setCharacterEncoding(CHARSET_NAME);
        OutputStream out = response.getOutputStream();
        response.setContentType("application/pdf");
        String fileName = String.format("%s.pdf", fontNameParam + "-" + System.currentTimeMillis());
        response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(fileName, CHARSET_NAME));
        export(fontNameParam, new HashMap<>(), "pdf.html", out);
    }

    public void export(String fontNameParam, Map<String, Object> paramMap, String templateName, OutputStream out) throws Exception {
        try {
            String templateHtml = generate(templateName, paramMap);
            this.generatePdf(fontNameParam, templateHtml, out);
        } catch (Throwable var5) {
            throw var5;
        }
    }

    public static String generate(String template, Map<String, Object> variables) throws Exception {
        Configuration config = FreemarkerConfiguration.getConfiguation();
        Template tp = config.getTemplate(template);
        StringWriter stringWriter = new StringWriter();
        BufferedWriter writer = new BufferedWriter(stringWriter);
        tp.setEncoding("UTF-8");
        tp.process(variables, writer);
        String htmlStr = stringWriter.toString();
        writer.flush();
        writer.close();
        return htmlStr;
    }



    private void generatePdf(String fontNameParam, String htmlStr, OutputStream out) throws IOException, DocumentException {
        MyFontProvider myFontProvider =  new MyFontProvider(fontNameParam);
        Document document = new Document(PageSize.A4, 30.0F, 30.0F, 30.0F, 30.0F);
        document.setMargins(30.0F, 30.0F, 30.0F, 30.0F);
        PdfWriter writer = PdfWriter.getInstance(document, out);
        document.open();
        HtmlPipelineContext htmlContext = new HtmlPipelineContext(new CssAppliersImpl(myFontProvider)) {
            public HtmlPipelineContext clone() throws CloneNotSupportedException {
                HtmlPipelineContext context = super.clone();

                try {
                    ImageProvider imageProvider = this.getImageProvider();
                    context.setImageProvider(imageProvider);
                } catch (NoImageProviderException var3) {
                }

                return context;
            }
        };
        htmlContext.setImageProvider(new AbstractImageProvider() {
            public String getImageRootPath() {
                return "";
            }

            public Image retrieve(String src) {
                if (StringUtils.isEmpty(src)) {
                    return null;
                } else {
                    try {
                        Image image = Image.getInstance(src);
                        image.setAbsolutePosition(400.0F, 400.0F);
                        if (image != null) {
                            this.store(src, image);
                            return image;
                        }
                    } catch (Throwable var3) {
                        var3.printStackTrace();
                    }

                    return super.retrieve(src);
                }
            }
        });
        htmlContext.setAcceptUnknown(true).autoBookmark(true).setTagFactory(Tags.getHtmlTagProcessorFactory());
        CSSResolver cssResolver = XMLWorkerHelper.getInstance().getDefaultCssResolver(true);
        cssResolver.setFileRetrieve(new FileRetrieve() {
            public void processFromStream(InputStream in, ReadingProcessor processor) throws IOException {
                try {
                    InputStreamReader reader = new InputStreamReader(in, "UTF-8");
                    Throwable var4 = null;

                    try {
                        boolean var5 = true;

                        int i;
                        while(-1 != (i = reader.read())) {
                            processor.process(i);
                        }
                    } catch (Throwable var14) {
                        var4 = var14;
                        throw var14;
                    } finally {
                        if (reader != null) {
                            if (var4 != null) {
                                try {
                                    reader.close();
                                } catch (Throwable var13) {
                                    var4.addSuppressed(var13);
                                }
                            } else {
                                reader.close();
                            }
                        }

                    }
                } catch (Throwable var16) {
                }

            }

            public void processFromHref(String href, ReadingProcessor processor) throws IOException {
                URL url = new URL(href);
                HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                InputStream is = conn.getInputStream();

                try {
                    InputStreamReader reader = new InputStreamReader(is, "UTF-8");
                    Throwable var7 = null;

                    try {
                        boolean var8 = true;

                        int i;
                        while(-1 != (i = reader.read())) {
                            processor.process(i);
                        }
                    } catch (Throwable var17) {
                        var7 = var17;
                        throw var17;
                    } finally {
                        if (reader != null) {
                            if (var7 != null) {
                                try {
                                    reader.close();
                                } catch (Throwable var16) {
                                    var7.addSuppressed(var16);
                                }
                            } else {
                                reader.close();
                            }
                        }

                    }
                } catch (Throwable var19) {
                    var19.printStackTrace();
                }

            }
        });
        HtmlPipeline htmlPipeline = new HtmlPipeline(htmlContext, new PdfWriterPipeline(document, writer));
        Pipeline<?> pipeline = new CssResolverPipeline(cssResolver, htmlPipeline);
        XMLWorker worker = null;
        worker = new XMLWorker(pipeline, true);
        XMLParser parser = new XMLParser(true, worker, Charset.forName("UTF-8"));
        InputStream inputStream = new ByteArrayInputStream(htmlStr.getBytes());
        Throwable var12 = null;

        try {
            parser.parse(inputStream, Charset.forName("UTF-8"));
        } catch (Throwable var21) {
            var12 = var21;
            throw var21;
        } finally {
            if (inputStream != null) {
                if (var12 != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable var20) {
                        var12.addSuppressed(var20);
                    }
                } else {
                    inputStream.close();
                }
            }

        }
        document.close();
    }

}
