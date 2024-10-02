package kr.co.idgenerator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ResultWriter {
    public static void exportToExcel(final Map<String, List<BenchmarkResult>> results) throws Exception {
        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Benchmark Results");

        // Write data
        writeData(sheet, results);

        // Create charts
        createGroupedChart(workbook, sheet, "Generation Time (ms)", 2);
        createGroupedChart(workbook, sheet, "Collision Rate", 4);
        createGroupedChart(workbook, sheet, "DB Join Time (ms)", 5);

        // Auto-size columns
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        // Write to file
        try (FileOutputStream outputStream = new FileOutputStream("BenchmarkResults.xlsx")) {
            workbook.write(outputStream);
        }
    }

    private static void writeData(Sheet sheet, Map<String, List<BenchmarkResult>> results) {
        System.out.println("Writing data to Excel...");
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Generator Name");
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
        System.out.println("Data written successfully.");
    }

    private static void createGroupedChart(XSSFWorkbook workbook, XSSFSheet sheet, String title, int dataColumn) {
        System.out.println("Creating chart: " + title);

        XSSFDrawing drawing = sheet.createDrawingPatriarch();
        XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, dataColumn + 4, 1, dataColumn + 15, 20); // 여기서 테이블 크기 조정

        XSSFChart chart = drawing.createChart(anchor);
        chart.setTitleText(title);
        chart.setTitleOverlay(false);

        XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
        bottomAxis.setTitle("Sample Size");
        XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
        leftAxis.setTitle(title);

        XDDFChartData data = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);

        XDDFChartData.Series uuidSeries = data.addSeries(getXDDFDataSource(sheet, "UUID"), getYDDFDataSource(sheet, "UUID", dataColumn));
        uuidSeries.setTitle("UUID", null);
        XDDFChartData.Series ulidSeries = data.addSeries(getXDDFDataSource(sheet, "ULID"), getYDDFDataSource(sheet, "ULID", dataColumn));
        ulidSeries.setTitle("ULID", null);
        XDDFChartData.Series ksuidSeries = data.addSeries(getXDDFDataSource(sheet, "KSUID"), getYDDFDataSource(sheet, "KSUID", dataColumn));
        ksuidSeries.setTitle("KSUID", null);

        chart.plot(data);

        XDDFBarChartData bar = (XDDFBarChartData) data;
        bar.setBarDirection(BarDirection.COL);
        bar.setBarGrouping(BarGrouping.CLUSTERED);

        System.out.println("Chart created successfully. about " + title);
    }

    private static XDDFCategoryDataSource getXDDFDataSource(XSSFSheet sheet, String generatorName) {
        System.out.println("Creating XDDFDataSource for " + generatorName);

        List<String> categories = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);

            if (row.getCell(0).getStringCellValue().equals(generatorName)) {
                categories.add(String.valueOf(row.getCell(1).getNumericCellValue()));
            }
        }
        return XDDFDataSourcesFactory.fromArray(categories.toArray(new String[0]));
    }

    private static XDDFNumericalDataSource<Double> getYDDFDataSource(XSSFSheet sheet, String generatorName, int dataColumn) {
        System.out.println("Creating YDDFDataSource for " + generatorName);

        List<Double> values = new ArrayList<>();
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row.getCell(0).getStringCellValue().equals(generatorName)) {
                values.add(row.getCell(dataColumn).getNumericCellValue());
            }
        }
        return XDDFDataSourcesFactory.fromArray(values.toArray(new Double[0]));
    }
}