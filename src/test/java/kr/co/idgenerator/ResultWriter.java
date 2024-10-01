package kr.co.idgenerator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

public class ResultWriter {
    public static void exportToExcel(final Map<String, List<BenchmarkResult>> results) throws Exception {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Benchmark Results");

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("ID Type");
        headerRow.createCell(1).setCellValue("Sample Size");
        headerRow.createCell(2).setCellValue("Generation Time (ms)");
        headerRow.createCell(3).setCellValue("Sortable");
        headerRow.createCell(4).setCellValue("Collision Rate");
        headerRow.createCell(5).setCellValue("DB Join Time (ms)");

        int rowNum = 1;
        for (Map.Entry<String, List<BenchmarkResult>> entry : results.entrySet()) {
            for (BenchmarkResult result : entry.getValue()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(result.getGeneratorName());
                row.createCell(1).setCellValue(result.getSampleSize());
                row.createCell(2).setCellValue(result.getGenerationTime());
                row.createCell(3).setCellValue(result.isSortable());
                row.createCell(4).setCellValue(result.getCollisionRate());
                row.createCell(5).setCellValue(result.getDbJoinTime());
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream("BenchmarkResults.xlsx")) {
            workbook.write(outputStream);
        }
    }
}
