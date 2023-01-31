//
// Translated by CS2J (http://www.cs2j.com): 30/01/2023 14:06:34
//

package HPASharp;


public class Size
{
    public Size(int width, int height) throws Exception {
        setWidth(width);
        setHeight(height);
    }

    private int __Height = new int();
    public int getHeight() {
        return __Height;
    }

    public void setHeight(int value) {
        __Height = value;
    }

    private int __Width = new int();
    public int getWidth() {
        return __Width;
    }

    public void setWidth(int value) {
        __Width = value;
    }

}
