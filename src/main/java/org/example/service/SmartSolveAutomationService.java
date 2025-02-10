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
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

        System.out.println("Total Pages: " + totalPages.getText());
        System.out.println("Total Items: " + totalItems.getText());

        JavascriptExecutor js = (JavascriptExecutor) webDriver;
        js.executeScript("arguments[0].click();", nextPageButton);
        System.out.println("Clicked the next page button!");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end!");
    }
}