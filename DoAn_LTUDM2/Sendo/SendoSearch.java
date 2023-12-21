package testPackage;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class SendoSearch {

	public static void main(String[] args) {
		//ChromeDriver path
		boolean flag=false;
		String path="C:\\Users\\Admin\\Downloads\\chromedriver-win64\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", path);
				
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--window-size=1980,1030");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-dev-shm-usage");
		WebDriver driver = new ChromeDriver(options);
		//Product url
		String url="https://www.sendo.vn/tim-kiem?q=%C4%91%E1%BB%93ng%20h%E1%BB%93";
		driver.get(url);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		//scrollDown(driver);
		try {
			waitForLoad(driver,"#root");
			Thread.sleep(3000);
			//WebElement searchDiv = driver.findElement(By.className("_0032-JlKwRS"));
			List<String> productUrls = new ArrayList<String>();
			List<ProductInfo> productInfos = new ArrayList<ProductInfo>();
			//List<Callable<List<Review>>> tasks = new ArrayList<>();
			List<Future<List<Review>>> futures = new ArrayList<>();
			//WebElement swiperWrapper = driver.findElement(By.className("swiper-wrapper"));
			WebElement productWrapper = driver.findElement(By.className("d7ed-mPGbtR"));
			flag = true;
			List<WebElement> productList = productWrapper.findElements(By.className("d7ed-d4keTB"));
			for(int i = 0; i < Math.min(productList.size(), 3); i++) {
				WebElement productDiv = productList.get(i);
				String productLink = productDiv.findElement(By.tagName("a")).getAttribute("href");
				productUrls.add(productLink);
				WebElement imgElement = productDiv.findElement(By.tagName("img"));
				String img = imgElement.getAttribute("src");
				//System.out.println("Product " + i + " image: " + img);
				String title = imgElement.getAttribute("alt");
				//System.out.println("Product " + i + " title: " + title);
				WebElement priceElement = productDiv.findElement(By.className("d7ed-AHa8cD"));
				String[] priceTag = priceElement.getText().split("đ");  
				String price = priceTag[0];
				//System.out.println("Product " + i + " price: " + price);
				WebElement ratingElement = productDiv.findElement(By.className("d7ed-bjQW4F"));
				String[] ratingScore = ratingElement.getText().split("/"); 
				String rating = ratingScore[0];
				//System.out.println("Product " + i + " rating: " + rating);
				ProductInfo product = new ProductInfo(img,title,Integer.parseInt(price.replace(".","")),Integer.parseInt(rating.replace(".","")));
				productInfos.add(product);
			}
				//getProductInformation(productInfos);
			ExecutorService executorService = Executors.newFixedThreadPool(productUrls.size());
			try {
	            for (int i = 0; i < productUrls.size(); i++) {
	                final int index = i;
	                Future<List<Review>> future = executorService.submit(() -> GetReview(productUrls.get(index)));
	                futures.add(future);
	            }
	            for (int i = 0; i < productUrls.size(); i++) {
	                try {
	                    List<Review> reviews = futures.get(i).get();
	                    productInfos.get(i).setReviews(reviews);
	                }catch(Exception e) {
	                	e.printStackTrace();
	                }
	            }
	        } finally {
	            executorService.shutdown();
	        }
			/*for (String productUrl : productUrls) {
	            tasks.add(() -> GetReview(url));
	        }
			try {
	            List<Future<List<Review>>> futures = executorService.invokeAll(tasks);

	            for (int i = 0; i < futures.size(); i++) {
	                Future<List<Review>> future = futures.get(i);
	                List<Review> reviews = future.get();
	                for(int j=0;j<reviews.size();j++) {
	                	System.out.println(reviews.get(i).getName());
	                }
	                
	                // Store or process the reviews as needed
	                productInfos.get(i).setReviews(reviews);
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	            executorService.shutdown();
	        }*/
			getProductInformation(productInfos);
			}catch(Exception e) {
					System.out.println("Co loi xay ra");
					e.printStackTrace();
				}finally {
					System.out.println("flag: "+flag);
					driver.quit();
				}
		
	}
	public static void waitForLoad(WebDriver driver,String component) {
		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(30));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(component)));
	}
	private static void scrollDown(WebDriver driver) {
        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
        jsExecutor.executeScript("window.scrollTo(0, document.documentElement.scrollHeight/2);");
    }
	private static void getProductInformation(List<ProductInfo> products) {
		for(int i=0;i<products.size();i++) {
			products.get(i).getProductInfo();
		}
	}
	private static List<Review> GetReview(String url) {
		String path="C:\\Users\\Admin\\Downloads\\chromedriver-win64\\chromedriver.exe";
		System.setProperty("webdriver.chrome.driver", path);
		List<Review> reviews = new ArrayList<Review>(); 
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless");
		options.addArguments("--no-sandbox");
		options.addArguments("--window-size=1980,1030");
		options.addArguments("--disable-infobars");
		options.addArguments("--disable-extensions");
		options.addArguments("--disable-dev-shm-usage");
		WebDriver driver = new ChromeDriver(options);
		driver.get(url);
		JavascriptExecutor js = (JavascriptExecutor) driver;
		//scrollDown(driver);
		try {
			waitForLoad(driver,"#root");
			Thread.sleep(3000);
			List<WebElement> commentSections = driver.findElements(By.className("_39ab-aJ_2cA"));
			for (int i = 0; i < Math.min(commentSections.size(), 3); i++) {
	            WebElement commentSection = commentSections.get(i);
	            // Extract Name
	            String name = commentSection.findElement(By.className("_39ab-RycCgu")).getText();
	            // Extract Comment
	            String comment = commentSection.findElement(By.className("_39ab-_2vzod")).findElement(By.tagName("p")).getText();
	            // Extract src of the first image
	            String image = commentSection.findElements(By.tagName("img")).get(0).getAttribute("src");
	            Review review = new Review(name,comment,image);
	            reviews.add(review);
			}
			return reviews;
			
		}catch(Exception e) {
			System.out.println("An error occured");
			return reviews;
		}finally {
			driver.quit();
		}
	}
}

