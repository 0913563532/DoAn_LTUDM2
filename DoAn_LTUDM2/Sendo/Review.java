package testPackage;

public class Review {
	private String name;
	private String comment;
	private String img;
	public Review(String name,String comment, String img) {
    	this.name = name;
        this.comment = comment;
        this.img = img;
    }
	public String getName() {
		return name;
	}
	public String getComment() {
		return comment;
	}
	public String getImg() {
		return img;
	}
	public void getReview() {
		System.out.println("\nName: "+getName()+"\nComment: "+getComment()+"\nImage: "+getImg());
	}
}
