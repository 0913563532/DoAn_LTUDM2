package client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import model.ProductInfo;
import model.Review;
public class View implements Runnable{
	private static boolean isClosed = false;
	public static SecretKey aesKey;
	public static ConnectToServer connect;
	public static ObjectOutputStream out;
	public static ObjectInputStream in;
	public static String nameClient;
	
	
	 public View (ConnectToServer connect,String nameClient,ObjectOutputStream out, ObjectInputStream in) {
		    this.connect = connect;
		    this.nameClient = nameClient;
		    this.out = out;
		    this.in = in;
		    }
	@Override
    public void run() {
		try 
				
		{
			aesKey = generateAESKey(256);
			PublicKey publickey = (PublicKey)in.readObject();
			System.out.println("đã nhận");
			byte[] encrypt = encryptAesKey(publickey, aesKey);
			System.out.println("đã mã hóa");
			out.writeObject(encrypt);
			out.flush();
			System.out.println("đã gửi");
			createAndShowGUI(connect);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
    public static void createAndShowGUI(ConnectToServer connect) {

        JFrame frame = new JFrame(nameClient);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                isClosed = true;
                try {
                	
					connect.close(in, out);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
            }
        });
        frame.setSize(1300, 1000);
        frame.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout());
        frame.add(mainPanel);

        // Create a search bar and search button
        JTextField searchField = new JTextField(30);
        JButton searchButton = new JButton("Search");
        JPanel searchPanel = new JPanel();
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        mainPanel.add(searchPanel, BorderLayout.NORTH);

        // Create a productsPanel to hold the product cards
        JPanel productsPanel = new JPanel();
        productsPanel.setLayout(new GridLayout(0, 3)); // 3 columns

        // Create a JScrollPane to make the productsPanel scrollable
        JScrollPane scrollPane = new JScrollPane(productsPanel);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.setVisible(true);

        // Add action listener to the search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	//xóa panel trước đó
            	productsPanel.removeAll();
                productsPanel.revalidate();
                productsPanel.repaint();
            
                // Implement the logic to send the search query to the server
                // and update the productsPanel with the search results.
                try {
                
                String input=searchField.getText();
                System.out.println(searchField.getText());
                
               
                
               
				
				
//               out = new ObjectOutputStream(connect.getSocket().getOutputStream());
               out.writeObject(encrypt(input, aesKey));
                out.flush();
                System.out.println(searchField.getText());
//               in = new ObjectInputStream(connect.getSocket().getInputStream());
                
               System.out.println(nameClient+" đang lấy dữ liệu");
               
                List<ProductInfo> products = (List<ProductInfo>) in.readObject();
                List<Review> reviews = (List<Review>) in.readObject();
                
                System.out.println(nameClient+" đã lấy");
                System.out.println(products.size());
                System.out.println(reviews.size());
                
                for (int i = 0; i < products.size(); i++) {
                	
                    ProductInfo product = products.get(i);  
                    
                    System.out.println("+ " +decrypt(product.getName(), aesKey) );
//                    System.out.println(product.getImg());
                   
                    Review review = reviews.get(i);
                    
//                    System.out.println(review.getImg());
                   
                    createProductCards(productsPanel,
                    		decrypt(product.getName(), aesKey),
                    		decrypt(product.getImg(), aesKey) ,
                    		decrypt(product.getPrice(), aesKey) ,
                    		decrypt(product.getRating(), aesKey) ,
                    		reviews, i);
                }
              
            }catch(Throwable err){
                err.printStackTrace();
            }
                
                frame.revalidate();
              
            }
        });
    }

    private static void createProductCards(JPanel productsPanel, String name, String imgURL, String price, String rating, List<Review> reviews,int dem) throws IOException {
    	double ratingStar = (double) Double.parseDouble(rating);
    	
        JPanel productCard = new JPanel(new BorderLayout());

//        String ratingStars = getRatingStars(rating);


        ImageIcon imageIcon = new ImageIcon(new ImageIcon(new URL(imgURL)).getImage().getScaledInstance(390,400, Image.SCALE_SMOOTH));

        JLabel productImageLabel = new JLabel(imageIcon); // Sử dụng ImageIcon đã thiết lập kích thước
        JLabel productNameLabel = new JLabel(formatProductName(name));
        Font nameFont = new Font("Arial", Font.BOLD, 16);
        productNameLabel.setFont(nameFont);
        JLabel productPriceLabel = new JLabel("Price: " + price+" đ");
        Font priceFont = new Font("Arial", Font.BOLD, 16);
        productPriceLabel.setFont(priceFont);
//        JLabel productRatingLabel = new JLabel("Rating: " + ratingStars);
//        Font ratingFont = new Font("", Font.BOLD, 16);
//        productRatingLabel.setFont(ratingFont);

        JPanel productDetailsPanel = new JPanel();
        productDetailsPanel.setLayout(new BoxLayout(productDetailsPanel, BoxLayout.Y_AXIS));
        productDetailsPanel.add(productImageLabel);
        productDetailsPanel.add(productNameLabel);
        productDetailsPanel.add(productPriceLabel);
        if(ratingStar == 0) {
       	 JLabel productRatingLabel = new JLabel("Rating: sản phẩm chưa có đánh giá");
            Font ratingFont = new Font(null, Font.BOLD, 16);
            productRatingLabel.setFont(ratingFont);
            productDetailsPanel.add(productRatingLabel);
        }
        else {
       	 String ratingStars = getRatingStars(ratingStar);
       	 JLabel productRatingLabel = new JLabel("rating"+ratingStars);
            Font ratingFont = new Font(null, Font.BOLD, 16);
            productRatingLabel.setFont(ratingFont);
            productDetailsPanel.add(productRatingLabel);
        }
       
        productCard.add(productDetailsPanel, BorderLayout.CENTER);

        productCard.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        productsPanel.add(productCard);
//		Hiển thị chi tiết sản phẩm
        productCard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
              try {
				createProductDetail(name, imgURL, price, rating, reviews,dem);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
            }
        });
    }

    

  
    
    private static void createProductDetail( String name, String imgURL, String price, String rating,List<Review> reviews,int dem) throws IOException {
    	 double ratingStar = (double) Double.parseDouble(rating);
    	 JFrame frame = new JFrame("Detail");
         frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         frame.setSize(1300, 1000);
         frame.setResizable(false);

         JPanel detail = new JPanel();
         detail.setBackground(Color.white);
         detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        
         
         ImageIcon imageIcon = new ImageIcon(new ImageIcon(new URL(imgURL)).getImage().getScaledInstance(495,600, Image.SCALE_SMOOTH));
         JLabel productImageLabel = new JLabel(imageIcon); // Sử dụng ImageIcon đã thiết lập kích thước
         JLabel productNameLabel = new JLabel(name);
         Font nameFont = new Font("Arial", Font.BOLD, 16);
         productNameLabel.setFont(nameFont);
         JLabel productPriceLabel = new JLabel(""+price);
         Font priceFont = new Font("Arial", Font.BOLD, 16);
         productPriceLabel.setFont(priceFont);
        
         JLabel tmp = new JLabel("Bình Luận");
         Font tmpFont = new Font(null, Font.BOLD, 16);
         tmp.setFont(tmpFont);
         detail.add(productImageLabel);
         detail.add(productNameLabel);
         detail.add(productPriceLabel);
         if(ratingStar == 0) {
        	 JLabel productRatingLabel = new JLabel("rating: sản phẩm chưa có đánh giá");
             Font ratingFont = new Font(null, Font.BOLD, 16);
             productRatingLabel.setFont(ratingFont);
             detail.add(productRatingLabel);
         }
         else {
        	 String ratingStars = getRatingStars(ratingStar);
        	 JLabel productRatingLabel = new JLabel("rating"+ratingStars);
             Font ratingFont = new Font(null, Font.BOLD, 16);
             productRatingLabel.setFont(ratingFont);
             detail.add(productRatingLabel);
         }
        
       
         
         detail.add(tmp);
//         JTextPane textContent = new JTextPane();
         
         JPanel comment = new JPanel();
         comment.setBackground(Color.white);
         comment.setLayout(new BoxLayout(comment, BoxLayout.Y_AXIS));
         for(Review review : reviews) {
        	 try {
				System.out.println(decrypt(review.getName(), aesKey));
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        	 String test=null;
        	 JLabel Jname = new JLabel();
        	 Font FontJname = new Font("Arial", Font.BOLD, 13);
            
        	 Jname.setFont(FontJname);
             JLabel Jimg = new JLabel();
             JLabel Jcomment = new JLabel();
             Font FontJcomment = new Font("Arial", Font.PLAIN, 12);
             
             Jcomment.setFont(FontJcomment);
        	 try {
    			String nameCreated = decrypt(review.getName(), aesKey);
    			String imgReview = decrypt(review.getImg(), aesKey);
    			//String cmt = decrypt(review.getComment(), aesKey);
    			String cmt = review.getComment();
        		 int LastTowNum = Integer.parseInt(  nameCreated.substring(nameCreated.length()-2));
        		 
        		 if (LastTowNum == dem) {
        			 
        			 nameCreated = nameCreated.substring(0,nameCreated.length()-2);
        			 Jname.setText(nameCreated);
        			 Jcomment.setText(cmt);
        			 if (!imgReview.equals("(không có hình)")) {
        			 ImageIcon imgCmt = new ImageIcon(new ImageIcon(new URL(imgReview)).getImage().getScaledInstance(80,80, Image.SCALE_SMOOTH));
        			 Jimg.setIcon(imgCmt);
        			 }
        			 else
        				 Jimg.setText("");
        			 
        		 }
        	
		
				
				
			} catch (Exception e) {
				e.printStackTrace();
			}
        	  comment.add(Jname);
              comment.add(Jimg);
              comment.add(Jcomment);
            
             
         }
         JScrollPane scrollPane = new JScrollPane(comment);
       detail.add(scrollPane);
       
         	

             
         
      
         frame.add(detail);
         frame.setVisible(true);
         
        
    	
    }
    private static String getRatingStars(double rating) {
        int roundedRating = (int) Math.round(rating);
        return "★".repeat(roundedRating);
    }
    
    private static String formatProductName(String name) {
    	 if (name.length() > 40) {
             return name.substring(0, 40) + "...";
         
        } else {
            return name;
        }
    }

    private static String wrapText(String input, int maxLength, int wrapPosition) {
        StringBuilder wrappedText = new StringBuilder();
        int currentLength = 0;

        for (String word : input.split("\\s+")) {
            // Check if adding the next word exceeds the maxLength
            if (currentLength + word.length() > maxLength) {
                wrappedText.append(System.lineSeparator());
                currentLength = 0;
            }

            // Add the word and update currentLength
            wrappedText.append(word).append(" ");
            currentLength += word.length() + 1;

            // Check if a line break is needed at the wrapPosition
            if (currentLength >= wrapPosition) {
                wrappedText.append(System.lineSeparator());
                currentLength = 0;
            }
        }

        return wrappedText.toString().trim();
    }
    public static byte[] encryptAesKey(PublicKey publicKey, SecretKey aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(aesKey.getEncoded());
    }
    public static SecretKey generateAESKey(int keySize) throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(keySize); // Key size can be 128, 192, or 256 bits
        return keyGenerator.generateKey();
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
//    public boolean isClosed() {
//        return isClosed;
//    }
    public static String encrypt(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Chuyển đổi mảng byte thành chuỗi sử dụng Base64
        String encryptedString = Base64.getEncoder().encodeToString(encryptedBytes);
        
        return encryptedString;
    }
}

