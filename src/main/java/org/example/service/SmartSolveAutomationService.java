package org.example.service;

import org.example.util.FileReaderUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.example.util.FileReaderUtil.readCSVFile;
import static org.example.util.FileReaderUtil.readExcelFile;

@Service
public class SmartSolveAutomationService {
    @Autowired
    private WebDriver webDriver;

    public void automateAndDownloadFile() {
        // 导航到网站
        webDriver.get("https://siemens.pilgrimasp.com/prod/smartsolve/Pages/Dashboard.aspx#/load?tabId=Home&searchPage=%7B-au-%7DPages/SmartPortal.aspx");
        // 等待1分钟 来让用户登录！ 测试发现 上午需要登录 下午不需要登录
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("user should login and load the home page!");

        // 跳转到RRU dashboard对应的界面
        webDriver.get("https://siemens.pilgrimasp.com/prod/smartsolve/Pages/Dashboard.aspx#/load?tabId=Home&searchPage=%7B-au-%7DPages/SmartPortal.aspx%7B-qm-%7DItmId%7B-eq-%7D0000000000000002147484680");
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("load the RRU Dashboard page!");

        // 切换到第一层iframe
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(15));
        WebElement iframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("search-pane-content-iframe")));
        webDriver.switchTo().frame(iframe);
        System.out.println("Switch to another frame!");

        // 切换到第二层iframe
        String secondIframeId = "wpManager_wp202144868_wp790622277_iframe1";
        WebElement secondIframe = wait.until(ExpectedConditions.presenceOfElementLocated(By.id(secondIframeId)));
        webDriver.switchTo().frame(secondIframe);
        System.out.println("Switch to the second frame!");
        // todo 移除export这个按键的步骤 可省略这个步骤 也可以减少等待时间
//        // 等待“export button”元素出现并可点击
//        WebElement exportButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/form/div[4]/div[7]/a")));
//        System.out.println("find button!");
//        // 点击“export button”
//        try {
//            Thread.sleep(15000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        // 使用 JavaScript 点击按钮
//        JavascriptExecutor js = (JavascriptExecutor) webDriver;
//        js.executeScript("arguments[0].click();", exportButton);
//        System.out.println("Clicked the export button!");
//
        //等待80s文件下载完毕以后 关闭浏览器
        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // todo 打印出当前页面的完整 HTML 内容
//        String pageSource = webDriver.getPageSource();
//        System.out.println(pageSource);
        //webDriver.quit();

        //读取已处理numbers文件的log
        String logFilePath = "C:\\Users\\z0052cmr\\IdeaProjects\\OverseasAutomationTool\\src\\main\\resources\\SiemensLog_Report_Number.csv";
        List<String> complaintNumbers = FileReaderUtil.readCSVFile(logFilePath);

        //2024-00078345-1 用于测试
        List<String> debugNumbers = new ArrayList<>();
        debugNumbers.add("2024-00078345-1");

        // 找到表格的 id="ctl05_resultGrid"的元素
        WebElement totalPages = webDriver.findElement(By.xpath("/html/body/form/div[4]/div[6]/table/tbody/tr/td/div/table[2]/tbody/tr[1]/td/div[2]/table/tfoot/tr/td/table/tbody/tr/td/div[5]/strong[2]/span"));
        WebElement totalItems = webDriver.findElement(By.xpath("/html/body/form/div[4]/div[6]/table/tbody/tr/td/div/table[2]/tbody/tr[1]/td/div[2]/table/tfoot/tr/td/table/tbody/tr/td/div[5]/strong[5]/span"));
        WebElement nextPageButton = webDriver.findElement(By.xpath("/html/body/form/div[4]/div[6]/table/tbody/tr/td/div/table[2]/tbody/tr[1]/td/div[2]/table/tfoot/tr/td/table/tbody/tr/td/div[3]/input[1]"));


        int totalPagesCount = Integer.parseInt(totalPages.getText());
        int totalItemsCount = Integer.parseInt(totalItems.getText());

        System.out.println("Total Pages: " + totalPagesCount);
        System.out.println("Total Items: " + totalItemsCount);

        for (int i = 1; i <= totalPagesCount; i++) {
            // 处理当前页面的元素 根据tbody获取所有的行
            // 找到表格的 tbody 的元素
            WebElement tbody = webDriver.findElement(By.xpath("/html/body/form/div[4]/div[6]/table/tbody/tr/td/div/table[2]/tbody/tr[1]/td/div[2]/table/tbody"));
            // 获取 div_resultGrid 下的所有 tr 元素 每个tr元素都是一个row
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));

            for (WebElement row : rows) {
                // 找到每行的第二个 td 元素（假设投诉编号在第二个 td 中）
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.size() > 1) {
                    WebElement cell_record_number = cells.get(1);
                    String cellText_record_number = cell_record_number.getText().trim();
                    WebElement cell_group = cells.get(4);
                    String cellText_group = cell_group.getText().trim();
                    if (cellText_record_number.length() > 0 && cellText_group.length() > 0) {
                        System.out.print(cellText_record_number);
                        System.out.print(" ");
                        System.out.println(cellText_group);
                    }
                    if(complaintNumbers.contains(cellText_record_number)) {
                        System.out.println("Complaint: " + cellText_record_number + " existed!");
                    }
                    // 检查文本内容是否与投诉编号匹配
//                    if (cellText.equals(complaintNumber)) {
//                        // 找到该行中的链接元素
//                        WebElement link = row.findElement(By.cssSelector("td div span a"));
//                        // 点击链接
//                        link.click();
//                        // 可以在这里添加等待页面加载的逻辑，例如使用 WebDriverWait
//                        try {
//                            Thread.sleep(30000); // 简单的等待 3 秒，实际使用中建议使用 WebDriverWait
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        break; // 找到匹配的行后跳出内层循环
//                    }
                }
            }
            if (i < totalPagesCount) {
                JavascriptExecutor js = (JavascriptExecutor) webDriver;
                js.executeScript("arguments[0].click();", nextPageButton);
                System.out.println("Clicked the next page button!");
                // 点击了next page button以后等待10s
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("end!");
    }
}