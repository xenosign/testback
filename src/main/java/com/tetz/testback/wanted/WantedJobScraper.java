package com.tetz.testback.wanted;

import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WantedJobScraper {

    private static class JobPosting {
        private String title;
        private String company;
        private String link;
        private List<String> techStack;

        public JobPosting(String title, String company, String link) {
            this.title = title;
            this.company = company;
            this.link = link;
            this.techStack = new ArrayList<>();
        }

        public void addTechStack(String tech) {
            techStack.add(tech);
        }

        @Override
        public String toString() {
            return "JobPosting [title=" + title + ", company=" + company + ", techStack=" + techStack + "]";
        }
    }

    public static void main(String[] args) {
        // URL to scrape
        String url = "https://www.wanted.co.kr/wdlist/518?country=kr&job_sort=job.latest_order&years=0&years=2&selected=660&selected=872&selected=873&locations=all";

        // Setup WebDriver using Chrome options
        ChromeOptions options = new ChromeOptions();
        // 기본 헤드리스 모드를 비활성화 - 디버깅을 위해
        // options.addArguments("--headless");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36");

        // Initialize WebDriver
        WebDriver driver = new ChromeDriver(options);
        List<JobPosting> allJobPostings = new ArrayList<>();

        try {
            // Navigate to the job listing page
            driver.get(url);
            System.out.println("Loaded page: " + driver.getTitle());

            // 페이지 구조를 확인하기 위해 현재 HTML을 출력
            System.out.println("Page source snippet: " + driver.getPageSource().substring(0, 1000) + "...");

            // 기다리는 시간을 늘려서 페이지가 완전히 로드될 시간을 줍니다
            Thread.sleep(5000);

            // 웹페이지의 CSS 선택자를 업데이트합니다 (더 일반적인 선택자 사용)
            // 1. 먼저 job-card 또는 비슷한 클래스가 있는지 확인
            List<WebElement> jobCards = null;

            // 여러 가능한 선택자를 시도
            String[] possibleSelectors = {
                    "ul.JobList_list__k2NKm li", // 원래 선택자
                    "div.job-card",              // 일반적인 job-card 클래스
                    "div[data-cy='job-card']",   // data-cy 속성
                    "div.job-item",              // 다른 가능한 클래스
                    "li.list-position",          // 리스트 아이템
                    "div.card",                  // 일반적인 카드 클래스
                    "article.job-posting"        // 기사 형태의 채용공고
            };

            for (String selector : possibleSelectors) {
                jobCards = driver.findElements(By.cssSelector(selector));
                if (jobCards != null && !jobCards.isEmpty()) {
                    System.out.println("Found elements with selector: " + selector);
                    System.out.println("Number of elements: " + jobCards.size());
                    break;
                }
            }

            if (jobCards == null || jobCards.isEmpty()) {
                // 직접 모든 앵커 태그를 찾아서 직업 포스팅 URL을 추출
                System.out.println("No job cards found with standard selectors. Trying to find job links directly.");
                List<WebElement> allLinks = driver.findElements(By.tagName("a"));
                List<String> jobLinks = new ArrayList<>();

                for (WebElement link : allLinks) {
                    String href = link.getAttribute("href");
                    if (href != null && href.contains("wanted.co.kr/wd/")) {
                        jobLinks.add(href);
                        System.out.println("Found job link: " + href);
                    }
                }

                System.out.println("Found " + jobLinks.size() + " job links directly");

                // 직접 찾은 링크를 이용해 처리
                processJobLinks(driver, jobLinks, allJobPostings);
            } else {
                // 카드 요소에서 정보 추출
                processJobCards(driver, jobCards, allJobPostings);
            }

            // Write the collected data to a CSV file
            writeToCSV(allJobPostings, "wanted_tech_stacks.csv");

        } catch (Exception e) {
            System.err.println("Error during scraping: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the browser
            driver.quit();
        }
    }

    private static void processJobLinks(WebDriver driver, List<String> jobLinks, List<JobPosting> allJobPostings) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        for (int i = 0; i < jobLinks.size(); i++) {
            try {
                String detailLink = jobLinks.get(i);

                // 새 탭에서 상세 페이지 열기
                ((JavascriptExecutor) driver).executeScript("window.open('" + detailLink + "', '_blank');");

                // 새 탭으로 전환
                ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(1));

                // 페이지 로드 대기
                wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));

                // 제목과 회사 정보 추출 (여러 가능한 선택자 시도)
                String title = "";
                String company = "";

                try {
                    title = driver.findElement(By.tagName("h1")).getText();
                } catch (Exception e) {
                    System.out.println("Could not find title with h1 tag");
                    try {
                        title = driver.findElement(By.cssSelector(".job-title")).getText();
                    } catch (Exception e2) {
                        title = "Unknown Title";
                    }
                }

                try {
                    company = driver.findElement(By.cssSelector(".company-name")).getText();
                } catch (Exception e) {
                    try {
                        company = driver.findElement(By.cssSelector("aside h6")).getText();
                    } catch (Exception e2) {
                        company = "Unknown Company";
                    }
                }

                JobPosting job = new JobPosting(title, company, detailLink);

                // 기술 스택 정보 추출
                extractTechStack(driver, job);

                // 수집된 데이터 추가
                allJobPostings.add(job);
                System.out.println("Processed " + (i+1) + "/" + jobLinks.size() + ": " + job);

                // 탭 닫고 원래 탭으로 돌아가기
                driver.close();
                driver.switchTo().window(tabs.get(0));

                // 서버 부하 방지를 위한 딜레이
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("Error processing job " + (i+1) + ": " + e.getMessage());
                e.printStackTrace();

                // 원래 탭으로 복귀
                ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
                if (tabs.size() > 1) {
                    driver.close();
                    driver.switchTo().window(tabs.get(0));
                }
            }
        }
    }

    private static void processJobCards(WebDriver driver, List<WebElement> jobCards, List<JobPosting> allJobPostings) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        for (int i = 0; i < jobCards.size(); i++) {
            try {
                // stale element 참조 방지를 위해 요소 다시 가져오기
                WebElement card = jobCards.get(i);

                // 카드에서 기본 정보 추출 (여러 가능한 선택자 시도)
                String title = "Unknown Title";
                String company = "Unknown Company";
                String detailLink = "";

                try {
                    // 제목 추출 시도
                    List<WebElement> titleElements = card.findElements(By.cssSelector("strong, h2, .job-title"));
                    if (!titleElements.isEmpty()) {
                        title = titleElements.get(0).getText();
                    }

                    // 회사명 추출 시도
                    List<WebElement> companyElements = card.findElements(By.cssSelector("span.company-name, span.job-card-company-name, .company"));
                    if (!companyElements.isEmpty()) {
                        company = companyElements.get(0).getText();
                    }

                    // 링크 추출 시도
                    WebElement linkElement = card.findElement(By.tagName("a"));
                    detailLink = linkElement.getAttribute("href");
                } catch (Exception e) {
                    System.err.println("Error extracting basic info from card: " + e.getMessage());
                    continue; // 기본 정보를 추출할 수 없으면 다음 카드로
                }

                if (detailLink.isEmpty()) {
                    System.out.println("No detail link found, skipping job");
                    continue;
                }

                JobPosting job = new JobPosting(title, company, detailLink);

                // 새 탭에서 상세 페이지 열기
                ((JavascriptExecutor) driver).executeScript("window.open('" + detailLink + "', '_blank');");

                // 새 탭으로 전환
                ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
                driver.switchTo().window(tabs.get(1));

                // 페이지 로드 대기
                Thread.sleep(2000);

                // 기술 스택 정보 추출
                extractTechStack(driver, job);

                // 수집된 데이터 추가
                allJobPostings.add(job);
                System.out.println("Processed " + (i+1) + "/" + jobCards.size() + ": " + job);

                // 탭 닫고 원래 탭으로 돌아가기
                driver.close();
                driver.switchTo().window(tabs.get(0));

                // 서버 부하 방지를 위한 딜레이
                Thread.sleep(1000);

            } catch (Exception e) {
                System.err.println("Error processing job " + (i+1) + ": " + e.getMessage());
                e.printStackTrace();

                // 원래 탭으로 복귀
                ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
                if (tabs.size() > 1) {
                    driver.close();
                    driver.switchTo().window(tabs.get(0));
                }
            }
        }
    }

    private static void extractTechStack(WebDriver driver, JobPosting job) {
        try {
            // 여러 가능한 선택자로 기술 스택 섹션 찾기
            List<WebElement> techSections = driver.findElements(
                    By.xpath("//h6[contains(text(), '기술스택') or contains(text(), '기술 스택') or " +
                            "contains(text(), '스킬') or contains(text(), '주요 기술') or " +
                            "contains(text(), 'Tech Stack') or contains(text(), '개발환경')]/following-sibling::*[1]")
            );

            if (!techSections.isEmpty()) {
                WebElement techStackSection = techSections.get(0);
                List<WebElement> techItems = techStackSection.findElements(By.tagName("li"));

                if (techItems.isEmpty()) {
                    // 리스트 형식이 아니면 텍스트 내용 가져오기
                    String techText = techStackSection.getText().trim();
                    if (!techText.isEmpty()) {
                        // 콤마나 닷으로 구분된 경우 분리
                        String[] techArray = techText.split("[,.]");
                        for (String tech : techArray) {
                            String trimmedTech = tech.trim();
                            if (!trimmedTech.isEmpty()) {
                                job.addTechStack(trimmedTech);
                            }
                        }
                    }
                } else {
                    for (WebElement techItem : techItems) {
                        job.addTechStack(techItem.getText().trim());
                    }
                }
            } else {
                // 특정 기술 스택 섹션을 찾을 수 없으면 설명 전체에서 기술 키워드 찾기
                extractTechStackFromDescription(driver, job);
            }
        } catch (Exception e) {
            System.err.println("Error extracting tech stack: " + e.getMessage());
            // 에러가 발생해도 설명에서 기술 키워드 시도
            extractTechStackFromDescription(driver, job);
        }
    }

    private static void extractTechStackFromDescription(WebDriver driver, JobPosting job) {
        // 일반적인 기술 스택 키워드
        Set<String> commonTechs = new HashSet<>();
        // 프로그래밍 언어
        commonTechs.add("Java"); commonTechs.add("Python"); commonTechs.add("JavaScript"); commonTechs.add("TypeScript");
        commonTechs.add("C++"); commonTechs.add("C#"); commonTechs.add("Go"); commonTechs.add("Ruby");
        commonTechs.add("PHP"); commonTechs.add("Swift"); commonTechs.add("Kotlin"); commonTechs.add("Scala");

        // 프레임워크와 라이브러리
        commonTechs.add("Spring"); commonTechs.add("Spring Boot"); commonTechs.add("Django"); commonTechs.add("Flask");
        commonTechs.add("React"); commonTechs.add("Angular"); commonTechs.add("Vue.js"); commonTechs.add("Node.js");
        commonTechs.add("Express"); commonTechs.add("Next.js"); commonTechs.add("Rails"); commonTechs.add(".NET");

        // 데이터베이스
        commonTechs.add("MySQL"); commonTechs.add("PostgreSQL"); commonTechs.add("MongoDB"); commonTechs.add("Oracle");
        commonTechs.add("SQL Server"); commonTechs.add("Redis"); commonTechs.add("Elasticsearch"); commonTechs.add("Cassandra");

        // 클라우드 플랫폼
        commonTechs.add("AWS"); commonTechs.add("Azure"); commonTechs.add("GCP"); commonTechs.add("Google Cloud");

        // DevOps 도구
        commonTechs.add("Docker"); commonTechs.add("Kubernetes"); commonTechs.add("Jenkins"); commonTechs.add("Git");

        // 모바일
        commonTechs.add("Android"); commonTechs.add("iOS"); commonTechs.add("React Native"); commonTechs.add("Flutter");

        try {
            // 전체 설명 텍스트 가져오기
            String descriptionText = "";
            try {
                descriptionText = driver.findElement(By.cssSelector("section.JobDescription")).getText();
            } catch (Exception e) {
                // 다른 선택자 시도
                try {
                    descriptionText = driver.findElement(By.cssSelector("article.job-description")).getText();
                } catch (Exception e2) {
                    // 본문 내용을 모두 가져오기
                    descriptionText = driver.findElement(By.tagName("body")).getText();
                }
            }

            // 설명에서 기술 키워드 찾기
            Set<String> foundTechs = new HashSet<>();
            for (String tech : commonTechs) {
                if (descriptionText.contains(tech)) {
                    foundTechs.add(tech);
                }
            }

            // 찾은 기술을 채용 공고에 추가
            for (String tech : foundTechs) {
                job.addTechStack(tech);
            }
        } catch (Exception e) {
            System.err.println("Error extracting tech from description: " + e.getMessage());
        }
    }

    private static void writeToCSV(List<JobPosting> jobs, String filename) throws IOException {
        FileWriter csvWriter = new FileWriter(filename);

        // 헤더 작성
        csvWriter.append("Title,Company,Link,Tech Stack\n");

        // 채용 공고 데이터 작성
        for (JobPosting job : jobs) {
            csvWriter.append(escapeCSV(job.title)).append(",");
            csvWriter.append(escapeCSV(job.company)).append(",");
            csvWriter.append(escapeCSV(job.link)).append(",");

            // 기술 스택 항목을 세미콜론으로 조인하여 한 CSV 셀에 유지
            String techStackStr = String.join("; ", job.techStack);
            csvWriter.append(escapeCSV(techStackStr));

            csvWriter.append("\n");
        }

        csvWriter.flush();
        csvWriter.close();

        System.out.println("CSV 파일이 성공적으로 생성되었습니다: " + filename);
    }

    private static String escapeCSV(String value) {
        // CSV 이스케이핑 처리
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\""); // 큰따옴표 이스케이프
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            escaped = "\"" + escaped + "\""; // 쉼표, 큰따옴표 또는 줄바꿈이 포함된 경우 따옴표로 감싸기
        }
        return escaped;
    }
}