package first.Project;

public class Product {
    private String name;
    private String shortDesc;
    private String longDesc;
    private int imageResId;
    private String price;

    public Product(String name, String shortDesc, String longDesc, int imageResId, String price) {
        this.name = name;
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
        this.imageResId = imageResId;
        this.price = price;
    }

    public String getName() { return name; }
    public String getShortDesc() { return shortDesc; }
    public String getLongDesc() { return longDesc; }
    public int getImageResId() { return imageResId; }
    public String getPrice() { return price; }
}
