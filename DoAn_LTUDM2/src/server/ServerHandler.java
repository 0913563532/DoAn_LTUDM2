package server;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.text.Normalizer;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import model.ProductInfo;
import model.Review;

public class ServerHandler implements Runnable {
	
	public ObjectOutputStream out;
	public ObjectInputStream in;
    private static Socket clientSocket;
    private  String clientName;
  
    public static List<ProductInfo> products = new ArrayList<>();
    public static List<Review> reviews= new ArrayList<>();
    public ServerHandler(Socket clientSocket,String clientName, ObjectOutputStream out , ObjectInputStream in) {
    	this.clientSocket = clientSocket;
    	this.clientName = clientName;
    	this.out = out;
    	this.in = in;
    }

    @Override
    public void run() {
  
    	SecretKey aesKey = null;  
//    	 Tạo cặp khóa RSA   	 
         KeyPair keyPair ;
     	 PublicKey publicKey = null;
         PrivateKey privateKey =null ;
         try {
			keyPair = generateKeyPair();
			publicKey = keyPair.getPublic();
			privateKey = keyPair.getPrivate();
         } catch (NoSuchAlgorithmException e1) {
 			
 			e1.printStackTrace();
 		}
    	 try {
    		 out.writeObject(publicKey);
	           out.flush();
	           out.reset();
	            System.out.println("đã gửi");
	            

	        		byte[] encrypted = (byte[]) in.readObject();
	    			System.out.println("đã nhận");
	    			 aesKey = decryptAesKey(encrypted, privateKey);
	             	System.out.println("đã giải mã");
	    	
    	 }catch (Exception e) {
			e.printStackTrace();
		}
    	 try {
    		 
		while(true) {
			System.out.println("key của "+clientName+" là " + aesKey);
    	String recv = "";

    		recv = decrypt((String)in.readObject(), aesKey) ;
    		
			System.out.println("Server đã nhận: " + recv+" từ client "+clientName);
    			
    			TikiSearch(recv,aesKey);
    			//SendoSearch(recv,aesKey);
//    			LazadaSearch(recv);
    			amazonSearch(recv,aesKey);
//    			System.out.println(reviews.size());;
//    			
//    			System.out.println(products.size());
//    			System.out.println(products.size());
    		
    			out.writeObject(products);
    			
    			
    			for(ProductInfo product : products) {
    				System.out.println(decrypt(product.getName(), aesKey));
    			}
    			out.writeObject(reviews);
    			out.flush();
    			out.reset();
    			products.clear();
    			reviews.clear();
    			
		
	
    
    	
	}
    	 }catch (Exception e) {
			System.out.println("client "+clientName+" đã đóng kết nối");
			e.printStackTrace();
			System.out.println(clientName+" "+aesKey.toString());
		}
    }
   void TikiSearch(String recv,SecretKey aesKey) throws Exception {
	   
	            	try {
	            		String apiSearchTiki = "https://tiki.vn/api/v2/products?limit=5&include=advertisement&aggregations=2&version=&trackity_id=a6ac1187-8267-0661-fd96-a4a7e2f02424&q=";
	            		apiSearchTiki +=recv;
	    				Document docSearchTiki = Jsoup.connect(apiSearchTiki)
	    						.method(Connection.Method.GET)
	    						.ignoreContentType(true)
	    						.execute()
	    						.parse();

	    				

	    				
	    				JSONObject jsonTiki = new JSONObject(docSearchTiki.text());
	    				// lấy 5 id sản phẩm từ trang tiki
	    				JSONArray jsonarray = jsonTiki.getJSONArray("data");
	    				int[] idProductTiki = new int[5];
	    				String[]img = new String[5];

	    			    for (int i = 0; i < 5; i++) {
	    		            JSONObject json =  jsonarray.getJSONObject(i);
	    		            System.out.println(json.getInt("id"));

	    		            idProductTiki[i] = json.getInt("id");
	    		            img[i] = json.getString("thumbnail_url");
	    		            
	    			    }
	    			    String apiProductTiki = "https://tiki.vn/api/v2/products/";
	    			    String apiContenProductTiki = "https://tiki.vn/api/v2/reviews?limit=5&product_id=";
	    			    		
	    			    
	    			    for (int i = 0; i < 5; i++) {
	    			    String tmp = apiProductTiki;
	    			    String tmp2 = apiContenProductTiki;
	    			    tmp = tmp+idProductTiki[i];
	    			    tmp2 = tmp2+idProductTiki[i];
	    				Document docProductTiki = Jsoup.connect(tmp)
	    						.method(Connection.Method.GET)
	    						.ignoreContentType(true)
	    						.execute()
	    						.parse();

	    				Document docReviews = Jsoup.connect(tmp2)
        						.method(Connection.Method.GET)
        						.ignoreContentType(true)
        						.execute()
        						.parse();

	    				JSONObject jsonProductTiki = new JSONObject(docProductTiki.text());
	    				JSONObject jsonReviews = new JSONObject(docReviews.text());
	    				JSONArray arrReviews = jsonReviews.getJSONArray("data");

	    				if (arrReviews.length()==0) {
	    					Review review = new Review(""+"0"+i,"","");
	    					reviews.add(review);

	    				}
	    				
	    				else {
	    				
	    				
	    				
        			    for (int j = 0; j < arrReviews.length()&&j<10 ; j++) {

        		            JSONObject jsonDataReview =  arrReviews.getJSONObject(j);
        		           
        		            String imgContent="";
    	    				String nameCreated="";
        		            String content = jsonDataReview.getString("content");
        		            
        		            JSONObject jsonNameCreated = jsonDataReview.getJSONObject("created_by");
        		            nameCreated = jsonNameCreated.getString("name");
        		            JSONArray jsonImgContent = jsonDataReview.getJSONArray("images");

        		            if (jsonImgContent.length()!=0) {
        		            	JSONObject jsonPath = jsonImgContent.getJSONObject(0) ;
        		            	imgContent = jsonPath.getString("full_path");
        		            	Review review = new Review(encrypt(nameCreated+"0"+i, aesKey), 
			        		            				   encrypt(content, aesKey), 
			        		            				   encrypt(imgContent, aesKey));
        		            	reviews.add(review);
        		            	
        		            }
        		            else {
        		            	imgContent="(không có hình)";
        		            	content="(không có bình luận)";
        		            	Review review = new Review(encrypt(nameCreated+"0"+i, aesKey), 
 		            				   encrypt(content, aesKey), 
 		            				   encrypt(imgContent, aesKey));
        		            	reviews.add(review);
        		            }
        		       	
	    					}
	    				}
	    			
        			    String nameP =""+ jsonProductTiki.getString("name");
        			    String priceP =""+ jsonProductTiki.getInt("price");
        			    String ratingP =""+ jsonProductTiki.getInt("rating_average");
	    				ProductInfo product = new ProductInfo(encrypt(img[i], aesKey) ,encrypt(nameP, aesKey),encrypt(priceP, aesKey) ,encrypt(ratingP, aesKey) );
	    				
	    				products.add(product);
	    				

	    			    }
	    			
	    			  
	            	}catch(IOException e){
	            		e.printStackTrace();
	            	}
   }
	  void LazadaSearch(String input) throws IOException {
		  	ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
			String formatText = removeAccent(input).replace(" ","-");
			//sử dụng input để lấy list sản phẩm cua Lazada
			String url= "https://www.lazada.vn/tag/" + formatText + "/?ajax=true&isFirstRequest=true&page=1";
			try {
				Document doc = Jsoup.connect(url)
					.method(Connection.Method.GET)
					.ignoreContentType(true)
					.execute()
					.parse();
				JSONObject jsonLazada = new JSONObject(doc.text());
				List<ProductInfo> products = new ArrayList<ProductInfo>();

				List<String> productUrls = new ArrayList<String>();
				List<Future<List<Review>>> futures = new ArrayList<>();
				//Lấy thông tin sản phẩm
					JSONArray jsonProductData = jsonLazada.getJSONObject("mods").getJSONArray("listItems");
					for(int i=0;i<Math.min(jsonProductData.length(),3);i++) {
						String name = jsonProductData.getJSONObject(i).getString("name");
						String img = jsonProductData.getJSONObject(i).getString("image");
						String price = jsonProductData.getJSONObject(i).getString("price");
						String rating = jsonProductData.getJSONObject(i).getString("ratingScore");
						String itemUrl = "https:" + jsonProductData.getJSONObject(i).getString("itemUrl");
						ProductInfo product = new ProductInfo(name,img,price,rating);
						productUrls.add(itemUrl);
						products.add(product);
						}
					ExecutorService executorService = Executors.newFixedThreadPool(productUrls.size());
					try {
			            for (int i = 0; i < productUrls.size(); i++) {
			                final int index = i;
			                Future<List<Review>> future = executorService.submit(() -> LazadaReviewScrap(productUrls.get(index)));
			                futures.add(future);
			            }
			            for (int i = 0; i < productUrls.size(); i++) {
			                try {
			                    List<Review> reviews = futures.get(i).get();
			                  products.get(i).setReviews(reviews);
			                  products.get(i).getProductInfo();
			                }catch(Exception e) {
			                	e.printStackTrace();
			                }
			            }
			        } finally {
			            executorService.shutdown();
			        }
			        //getProductInformation(products);
			}catch(Exception e) {
				System.out.println("Co loi xay ra");
//				e.printStackTrace();
			}
		
	  }
	  public static List<Review> LazadaReviewScrap(String url) throws IOException {
		  ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
		  String path="C:\\Users\\vansu\\Downloads\\chromedriver-win64\\chromedriver.exe";
			System.setProperty("webdriver.chrome.driver", path);
			List<String> userAgents = Arrays.asList(
					"Mozilla/5.0 (Linux; U; Linux i541 x86_64) AppleWebKit/537.10 (KHTML, like Gecko) Chrome/54.0.2762.307 Safari/601",
					"Mozilla/5.0 (Windows; Windows NT 10.5; x64) AppleWebKit/533.28 (KHTML, like Gecko) Chrome/52.0.3974.343 Safari/602",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_4_2) AppleWebKit/600.44 (KHTML, like Gecko) Chrome/49.0.2923.105 Safari/534",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_4; en-US) AppleWebKit/600.12 (KHTML, like Gecko) Chrome/54.0.3269.220 Safari/603",
					"Mozilla/5.0 (Linux; Linux i563 ; en-US) AppleWebKit/601.46 (KHTML, like Gecko) Chrome/52.0.3966.198 Safari/602",
					"Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_4_2; en-US) AppleWebKit/534.35 (KHTML, like Gecko) Chrome/55.0.1316.148 Safari/603",
					"Mozilla/5.0 (Linux; U; Linux x86_64) AppleWebKit/603.47 (KHTML, like Gecko) Chrome/49.0.2650.372 Safari/603"
					);
			String randomUserAgent = getRandomUserAgent(userAgents);
			ChromeOptions options = new ChromeOptions();

		    options.addArguments("--headless");
		    options.addArguments("--no-sandbox");
		    options.addArguments("--window-size=1980,1030");
		    options.addArguments("--disable-infobars");
		    options.addArguments("--disable-extensions");
		    options.addArguments("--disable-dev-shm-usage");
		    options.addArguments("--user-agent=" + randomUserAgent);
			WebDriver driver = new ChromeDriver();
			driver.get(url);
			List<Review> reviews = new ArrayList<Review>();
			JavascriptExecutor js = (JavascriptExecutor) driver;
			scrollDown(driver);
			try {
				waitForLoad(driver,"#root");
				WebElement modReviews = driver.findElement(By.className("mod-reviews"));			
				for (WebElement reviewItem : modReviews.findElements(By.className("item"))) {
		            // Lấy tên người dùng
		            String userName = reviewItem.findElement(By.cssSelector(".middle span")).getText();
		            // Review người dùng
		            String userReview = reviewItem.findElement(By.cssSelector(".item-content .content")).getText();
		            String img="";
		            try {
		            	WebElement imageDiv = modReviews.findElement(By.cssSelector(".review-image__item .image"));
		                String imgUrl=imageDiv.getAttribute("style");
		                // Lấy URL hình ảnh
		                String imageUrl = extractImageUrlFromStyleAttribute(imageDiv.getAttribute("style"));
		                img = imageUrl;
		            }catch(Exception e) {
		            	System.out.println("Image URL:"+"");
		            }
		            Review review = new Review(userName,userReview,img);
		            reviews.add(review);
		       }
				return reviews;
			}catch(Exception e) {
				System.out.println("Co loi xay ra");
				e.printStackTrace();
				return reviews;
			}finally {
				driver.quit();
			}
			
	  }
	  public static void SendoSearch(String input, SecretKey aesKey) {
		  
		  	String path="C:\\Users\\vansu\\Downloads\\chromedriver-win64\\chromedriver.exe";
			System.setProperty("webdriver.chrome.driver", path);
					
			ChromeOptions options = new ChromeOptions();
			options.addArguments("--headless");
			options.addArguments("--no-sandbox");
			options.addArguments("--window-size=1980,1030");
			options.addArguments("--disable-infobars");
			options.addArguments("--disable-extensions");
			options.addArguments("--disable-dev-shm-usage");
			WebDriver driver = new ChromeDriver(options);
			//Product url xử lý input
			String url="https://www.sendo.vn/tim-kiem?q="+input;
			driver.get(url);
			JavascriptExecutor js = (JavascriptExecutor) driver;
			//scrollDown(driver);
			try {
				waitForLoad(driver,"#root");
				Thread.sleep(3000);
				List<String> productUrls = new ArrayList<String>();
	
				List<Future<List<Review>>> futures = new ArrayList<>();
				WebElement productWrapper = driver.findElement(By.className("d7ed-mPGbtR"));
				List<WebElement> productList = productWrapper.findElements(By.className("d7ed-d4keTB"));
				for(int i = 0; i < Math.min(productList.size(), 5); i++) {
					WebElement productDiv = productList.get(i);
					//Lấy link sản phẩm
					String productLink = productDiv.findElement(By.tagName("a")).getAttribute("href");
					productUrls.add(productLink);
					WebElement imgElement = productDiv.findElement(By.tagName("img"));
					//Lấy hình ảnh
					String img = imgElement.getAttribute("src");
					//Lấy tiêu đề
					String title = imgElement.getAttribute("alt");
					//Lấy giá tiền
					WebElement priceElement = productDiv.findElement(By.className("d7ed-AHa8cD"));
					String[] priceTag = priceElement.getText().split("đ");  
					String price = priceTag[0];
					//Lấy điểm đánh giá
					WebElement ratingElement = productDiv.findElement(By.className("d7ed-bjQW4F"));
					String[] ratingScore = ratingElement.getText().split("/"); 
					String rating = ratingScore[0];
					//Thêm vào list product
					ProductInfo product = new ProductInfo(encrypt(img, aesKey) ,encrypt(title, aesKey) ,encrypt(price.replace(".",""), aesKey) ,encrypt(rating, aesKey) );
					products.add(product);
					
					
					
				}
//				xử lý cào review của các product bằng multi thread
				ExecutorService executorService = Executors.newFixedThreadPool(productUrls.size());
				try {
					for (int i = 0; i < productUrls.size(); i++) {
		                final int index = i;
		                Future<List<Review>> future = executorService.submit(() -> GetReviewSendo(productUrls.get(index),index ,aesKey));
		                System.out.println(index);
		                futures.add(future);
		            	
		            }
		            
					 for (int i = 0; i < productUrls.size(); i++) {
			                try {
			                    List <Review> tmp = futures.get(i).get();
			                    
			                    products.get(i).setReviews(tmp);

			                    products.get(i).getProductInfo();
			                }catch(Exception e) {
			                	e.printStackTrace();
			                }
			            }
					 for(int i =0 ; i <reviews.size() ; i++) {
						 
						 System.out.println(decrypt(reviews.get(i).getName(), aesKey) );
					 }
					
		            
		        } finally {
		        	
		            executorService.shutdown();
		        }
				//In thông tin sản phẩm

				
			}catch(Exception e) {
					System.out.println("Co loi xay ra");
					e.printStackTrace();
				}finally {
					driver.quit();
				}
		
	  }
	  public static String removeAccent(String s) {
	        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
	        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
	        return pattern.matcher(temp).replaceAll("");
	 }
	  private static String extractImageUrlFromStyleAttribute(String styleAttribute) {
	        try {
	        	String patternString = "url\\(\"(.*?)\"\\)";
	            Pattern pattern = Pattern.compile(patternString);
	            Matcher matcher = pattern.matcher(styleAttribute);
	            if (matcher.find()) {
	                String url = matcher.group(1);
	                return url;
	            }else {
	            	return "";
	            }
	        }catch(Exception e) {
	        	return "";
	        }
	    }
	  public static void waitForLoad(WebDriver driver,String component) {
			WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(100));
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(component)));
		}
		private static void scrollDown(WebDriver driver) {
	        JavascriptExecutor jsExecutor = (JavascriptExecutor) driver;
	        jsExecutor.executeScript("window.scrollTo(0, document.documentElement.scrollHeight/2);");
	    }
		
		private static List<Review> GetReviewSendo(String url, int dem, SecretKey aesKey) {
			String path="C:\\Users\\vansu\\Downloads\\chromedriver-win64\\chromedriver.exe";
			System.setProperty("webdriver.chrome.driver", path);
			List<Review> tmp = new ArrayList<Review>(); 
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
				for (int i = 0; i < Math.min(commentSections.size(), 5); i++) {
					
		            WebElement commentSection = commentSections.get(i);
		            // Extract Name
		            String	name = commentSection.findElement(By.className("_39ab-RycCgu")).getText()+"0"+(dem+5);
//		            System.out.println(name);
		            
		            // Extract Comment
		            String comment = commentSection.findElement(By.className("_39ab-_2vzod")).findElement(By.tagName("p")).getText();
		            // Extract src of the first image
		            String image;
		            try {
		            	image = commentSection.findElement(By.xpath(".//following-sibling::div[@class='_39ab-UBS3hL']"))
		                    .findElement(By.className("_39ab-ia6Ofv"))
		                    .findElement(By.tagName("img"))
		                    .getAttribute("src");
		            }catch(Exception e) {
		            	image = "(không có hình)";
		            }
//		          System.out.println(image);
		            Review review = new Review(encrypt(name, aesKey) ,encrypt(comment, aesKey) ,encrypt(image, aesKey) );
		            reviews.add(review);
		            tmp.add(review);
		           
		    
		            
				}

	
				return tmp;
				
			}catch(Exception e) {
				System.out.println("An error occured");
				return reviews;
			}finally {
				driver.quit();
			}
		}
		private static void amazonSearch(String input, SecretKey aesKey) {
		
			String formatedInput= convertedFormat(Translator(input));
			System.out.println(formatedInput);
			String url = "";
			try {
				String searchUrl = "https://www.amazon.com/s?k="+formatedInput+"&crid=P3RPA2NKGX6Z&sprefix=%2Caps%2C298&ref=nb_sb_ss_recent_1_0_recent";
				url = "https://api.scraperapi.com/?api_key=243e06b82644630237e09ec0d9745b33&url="
						+ URLEncoder.encode(searchUrl, "UTF-8") + "&autoparse=true";
			} catch (Exception e) {
				e.printStackTrace();
			}
			try {
				Document doc = Jsoup.connect(url).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
		
				JSONObject amazonProduct = new JSONObject(doc.text());
				JSONArray productArray = amazonProduct.getJSONArray("results");
				for (int i = 0; i < Math.min(5, productArray.length()); i++) {
					JSONObject item = productArray.getJSONObject(i);
					String name = item.getString("name");
					String image = item.getString("image");
					String[] priceString = String.valueOf(item.getDouble("price") * 24000).split("\\.");
					String price = priceString[0];
					String rating = String.valueOf(item.getDouble("stars"));
					String asin = item.getString("asin");
					getAmazonReview(asin,i,aesKey);
					ProductInfo product = new ProductInfo(encrypt (image, aesKey) ,encrypt(name, aesKey) ,encrypt(price, aesKey) ,encrypt(rating, aesKey));
					products.add(product);
				}
//				getProductInformation(products);

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		private static void getAmazonReview(String input,int dem,SecretKey aesKey) {
			try {
				String url = "https://api.scraperapi.com/?api_key=243e06b82644630237e09ec0d9745b33&url=https%3A%2F%2Fwww.amazon.com%2Fproduct-reviews%2F"
						+ input + "&autoparse=true";
				Document doc = Jsoup.connect(url).method(Connection.Method.GET).ignoreContentType(true).execute().parse();
		
				JSONObject reviewList = new JSONObject(doc.text());
				JSONArray reviewArray = new JSONArray(reviewList.getJSONArray("reviews"));
				for (int i = 0; i < Math.min(5, reviewArray.length()); i++) {
					JSONObject item = reviewArray.getJSONObject(i);
					String name = item.getString("username");
					name=name+"  "+(dem+10);
					String image = "(không có hình)";
					String review = item.getString("review");
					Review customerReview = new Review(encrypt(name, aesKey) ,encrypt(review, aesKey),encrypt( image, aesKey));
					reviews.add(customerReview);
					
				}
				
			} catch (Exception e) {
				List<Review> reviews = null;
				
				e.printStackTrace();
			}
		}

		private static void getProductInformation(List<ProductInfo> products) {
			for (int i = 0; i < products.size(); i++) {
				products.get(i).getProductInfo();
			}
		}
		public static String Translator(String input) {
			String encodedInput="";
			try {
				encodedInput = URLEncoder.encode(input,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			HttpRequest request = HttpRequest.newBuilder()
					.uri(URI.create("https://google-translate1.p.rapidapi.com/language/translate/v2"))
					.header("content-type", "application/x-www-form-urlencoded")
					.header("Accept-Encoding", "application/gzip")
					.header("X-RapidAPI-Key", "b909050fa1msh634a2a45915303cp148b8cjsnb4c4775b650d")
					.header("X-RapidAPI-Host", "google-translate1.p.rapidapi.com")
					.method("POST", HttpRequest.BodyPublishers.ofString("q="+encodedInput+"&target=en&source=vi"))
					.build();
			HttpResponse<String> response;
			try {
				response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
				JSONObject translator = new JSONObject(response.body());
				String translatedText = translator.getJSONObject("data")
		                .getJSONArray("translations")
		                .getJSONObject(0)
		                .getString("translatedText");
				return translatedText;
			} catch (IOException e) {
				String translatedText = "";
				return translatedText;
			} catch (InterruptedException e) {
				e.printStackTrace();
				String translatedText = "";
				return translatedText;
			}
		}
		public static String convertedFormat(String input) {
			String transformedString = input.toLowerCase().replaceAll("[\\s-]+", "+");
			return transformedString;
		}
		private static String getRandomUserAgent(List<String> userAgents) {
	        Random random = new Random();
	        int index = random.nextInt(userAgents.size());
	        return userAgents.get(index);
	    }
		
		//mã hóa
		
		 private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		        keyPairGenerator.initialize(2048); // Độ dài của khóa
		        return keyPairGenerator.generateKeyPair();
		    }
		    private SecretKey decryptAesKey(byte[] encryptedAesKey, PrivateKey privateKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		        Cipher cipher = Cipher.getInstance("RSA");
		        cipher.init(Cipher.DECRYPT_MODE, privateKey);
		        byte[] decryptedBytes = cipher.doFinal(encryptedAesKey);

		        // Convert decrypted bytes to SecretKey
		        return new SecretKeySpec(decryptedBytes, 0, decryptedBytes.length, "AES");
		    }
		    public static String encrypt(String data, SecretKey key) throws Exception {
		        Cipher cipher = Cipher.getInstance("AES");
		        cipher.init(Cipher.ENCRYPT_MODE, key);
		        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
		        
		        // Chuyển đổi mảng byte thành chuỗi sử dụng Base64
		        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
		        
		        return encryptedString;
		    }
		    public static String decrypt(String encryptedData, SecretKey key) throws Exception {
		        // Chuyển đổi chuỗi Base64 thành mảng byte
		        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedData);
		        
		        Cipher cipher = Cipher.getInstance("AES");
		        cipher.init(Cipher.DECRYPT_MODE, key);
		        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
		        
		        // Chuyển đổi mảng byte thành chuỗi
		        String decryptedString = new String(decryptedBytes, StandardCharsets.UTF_8);
		        
		        return decryptedString;
		    }
		   

}
