package org.example.util;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FileReaderUtil {



    public static List<String> readCSVFile(String filePath) {
        List<String> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            String[] nextLine;
            boolean isHeader = true; // 剔除开头行
            while ((nextLine = reader.readNext()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }
                data.add(nextLine[0]);
            }
        } catch (IOException | CsvException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static List<List<String>> readExcelFile(String filePath) {
        List<List<String>> data = new ArrayList<>();
        System.out.println("开始读取Excel文件: " + filePath);
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("错误：文件不存在！");
            return data;
        }
        System.out.println("文件大小: " + file.length() + " bytes");
        
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = getWorkbook(fis)) {
            System.out.println("成功打开工作簿");
            // 获取第一个工作表
            Sheet sheet = workbook.getSheetAt(0);
            System.out.println("获取到工作表，总行数: " + sheet.getPhysicalNumberOfRows());
            
            // 遍历每一行
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                List<String> rowData = new ArrayList<>();
                // 遍历每一列
                Iterator<Cell> cellIterator = row.cellIterator();
                while (cellIterator.hasNext()) {
                    Cell cell = cellIterator.next();
                    // 根据单元格类型获取值
                    switch (cell.getCellType()) {
                        case STRING:
                            rowData.add(cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                rowData.add(cell.getDateCellValue().toString());
                            } else {
                                rowData.add(String.valueOf(cell.getNumericCellValue()));
                            }
                            break;
                        case BOOLEAN:
                            rowData.add(String.valueOf(cell.getBooleanCellValue()));
                            break;
                        case FORMULA:
                            rowData.add(cell.getCellFormula());
                            break;
                        default:
                            rowData.add("");
                    }
                }
                data.add(rowData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static Workbook getWorkbook(FileInputStream file) throws IOException {
        Workbook workbook;
        try {
            workbook = new XSSFWorkbook(file); // 尝试读取.xlsx文件
        } catch (Exception e) {
            file.getChannel().position(0); // 重置文件指针
            workbook = new HSSFWorkbook(file); // 尝试读取.xls文件
        }
        return workbook;
    }
}
