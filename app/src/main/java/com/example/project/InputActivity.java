package com.example.project;

import static androidx.core.app.ActivityCompat.startActivityForResult;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.text.PDFTextStripper;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;

import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.InputStream;

public class InputActivity extends AppCompatActivity {

    private EditText etContent;
    private Button btnChooseFile, btnAnalyze, btnRemoveFile;
    private View cardFilePreview;
    private TextView tvFileName;
    private String extractedText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        PDFBoxResourceLoader.init(getApplicationContext());

        etContent = findViewById(R.id.etContent);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnAnalyze = findViewById(R.id.btnAnalyze);
        btnRemoveFile = findViewById(R.id.btnRemoveFile);
        cardFilePreview = findViewById(R.id.cardFilePreview);
        tvFileName = findViewById(R.id.tvFileName);

        btnRemoveFile.setOnClickListener(v -> {
            extractedText = "";
            cardFilePreview.setVisibility(View.GONE);
            etContent.setVisibility(View.VISIBLE);
            etContent.setText("");
        });

        btnChooseFile.setOnClickListener(v -> {

            Intent intent =
                    new Intent(Intent.ACTION_OPEN_DOCUMENT);
            
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            String[] mimeTypes = {
                    "application/pdf",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "text/plain" // Text
            };

            intent.putExtra(
                    Intent.EXTRA_MIME_TYPES,
                    mimeTypes
            );

            startActivityForResult(
                    intent,
                    100
            );
        });

        btnAnalyze.setOnClickListener(v -> {

            String content;

            if (!extractedText.isEmpty()) {
                content = extractedText;
            } else {
                content =
                        etContent.getText()
                                .toString()
                                .trim();
            }

            if (content.isEmpty()) {

                Toast.makeText(
                        this,
                        "กรุณาใส่เนื้อหาหรือเลือกไฟล์",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            // เคลียร์ค่า extractedText หลังจากส่งข้อมูลไปแล้ว เพื่อไม่ให้ค้างในการใช้งานครั้งถัดไป
            String finalContent = content;
            extractedText = ""; 

            Intent intent =
                    new Intent(
                            InputActivity.this,
                            QuizActivity.class
                    );

            intent.putExtra(
                    "content",
                    finalContent
            );

            startActivity(intent);
        });
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            @Nullable Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode == 100
                && resultCode == RESULT_OK
                && data != null) {

            Uri uri = data.getData();

            if (uri != null) {

                readFile(uri);
            }
        }
    }

    private void readFile(Uri uri) {
        // แสดง Loading นิดนึงเพราะไฟล์ใหญ่อาจจะช้า
        Toast.makeText(this, "กำลังอ่านไฟล์...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String mimeType = getContentResolver().getType(uri);
                InputStream inputStream = getContentResolver().openInputStream(uri);

                if (inputStream == null) {
                    runOnUiThread(() -> Toast.makeText(this, "ไม่สามารถเปิดไฟล์ได้", Toast.LENGTH_SHORT).show());
                    return;
                }

                final String resultText;

                // ตรวจสอบจาก MimeType หรือนามสกุลไฟล์ถ้า MimeType ไม่ชัดเจน
                if ("application/pdf".equals(mimeType) || (uri.getPath() != null && uri.getPath().endsWith(".pdf"))) {
                    resultText = readPdf(inputStream);
                } else if ("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mimeType) || (uri.getPath() != null && uri.getPath().endsWith(".docx"))) {
                    resultText = readDocx(inputStream);
                } else if ("application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(mimeType) || (uri.getPath() != null && uri.getPath().endsWith(".pptx"))) {
                    resultText = readPptx(inputStream);
                } else if ("text/plain".equals(mimeType) || (uri.getPath() != null && uri.getPath().endsWith(".txt"))) {
                    resultText = readTextFile(inputStream);
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "ไม่รองรับไฟล์ประเภทนี้ (" + mimeType + ")", Toast.LENGTH_SHORT).show());
                    return;
                }

                runOnUiThread(() -> {
                    if (resultText != null && !resultText.trim().isEmpty()) {
                        extractedText = resultText;
                        
                        // 🔥 แสดง Card ไฟล์ และซ่อน EditText
                        etContent.setVisibility(View.GONE);
                        cardFilePreview.setVisibility(View.VISIBLE);
                        
                        // ดึงชื่อไฟล์มาแสดง
                        String name = "File Attached";
                        if (uri.getPath() != null) {
                            String path = uri.getPath();
                            name = path.substring(path.lastIndexOf('/') + 1);
                        }
                        tvFileName.setText(name);

                        Toast.makeText(this, "แนบไฟล์สำเร็จ", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "ไม่พบข้อความในไฟล์นี้", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "เกิดข้อผิดพลาด: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private String readTextFile(InputStream inputStream) {
        StringBuilder sb = new StringBuilder();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String readPdf(
            InputStream inputStream
    ) {

        try {

            PDDocument document =
                    PDDocument.load(inputStream);

            PDFTextStripper stripper =
                    new PDFTextStripper();

            String text =
                    stripper.getText(document);

            document.close();

            return text;

        } catch (Exception e) {

            e.printStackTrace();

            return "";
        }
    }

    private String readDocx(
            InputStream inputStream
    ) {

        StringBuilder text =
                new StringBuilder();

        try {

            XWPFDocument document =
                    new XWPFDocument(inputStream);

            for (XWPFParagraph p :
                    document.getParagraphs()) {

                text.append(
                        p.getText()
                );

                text.append("\n");
            }

            document.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return text.toString();
    }

    private String readPptx(
            InputStream inputStream
    ) {

        StringBuilder text =
                new StringBuilder();

        try {

            XMLSlideShow ppt =
                    new XMLSlideShow(inputStream);

            for (XSLFSlide slide :
                    ppt.getSlides()) {

                if (slide.getTitle() != null) {

                    text.append(
                            slide.getTitle()
                    );

                    text.append("\n");
                }

                for (XSLFShape shape :
                        slide.getShapes()) {

                    if (shape instanceof XSLFTextShape) {

                        text.append(
                                ((XSLFTextShape) shape)
                                        .getText()
                        );

                        text.append("\n");
                    }
                }

                text.append("\n\n");
            }

            ppt.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return text.toString();
    }
}
