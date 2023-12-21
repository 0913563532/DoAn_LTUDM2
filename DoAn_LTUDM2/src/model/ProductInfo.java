package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ProductInfo implements Serializable{
	private String img;
    private String name;
    private String price;
    private String rating;
    private List<Review> reviews;

    public ProductInfo(String img, String name, String price,String rating) {
    	this.img=img;
    	this.name=name;
    	this.price=price;
    	this.rating=rating;
    	this.reviews=new ArrayList<>();
    }
    public String getImg() {
    	return img;
    }
    public String getName() {
    	return name;
    }
    public String getPrice() {
    	return price;
    }
    public String getRating() {
    	return rating;
    }
    public void getReviews() {
    	for(int i=0;i<reviews.size();i++) {
    		reviews.get(i).getReview();
    	}
    }
    public void setReviews(List<Review> reviews) {
    	this.reviews=reviews;
    }
   
    public void getProductInfo() {
    	System.out.println("Product name: "+getName()+"\nPrice: "+getPrice()+"\nrating: "+getRating()+"\nReviews:");
    	getReviews();
    }
}
