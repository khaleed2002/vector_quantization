import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

class Scratch {
    public static void printCodeBook(int mat[][]){
        for(int i = 0; i <mat.length; i++){
            System.out.print("[");
            fileWrite("codebook.txt","[");
            for(int j = 0; j<mat[0].length; j++){
                System.out.print(" " + mat[i][j] + " ");
                fileWrite("codebook.txt"," " + mat[i][j] + " ");
            }
            System.out.print("]");
            fileWrite("codebook.txt","]\n");
            System.out.println();
        }
        fileWrite("codebook.txt","===============\n");
        System.out.println("====================");
    }
    public static void printCompressedImage(String mat[][]){
        for(int i = 0; i <mat.length; i++){
            System.out.print("[");
            fileWrite("compressed.txt","[");
            for(int j = 0; j<mat[0].length; j++){
                System.out.print(" " + mat[i][j] + " ");
                fileWrite("compressed.txt"," " + mat[i][j] + " ");
            }
            System.out.print("]");
            fileWrite("compressed.txt","]\n");
            System.out.println();
        }
        fileWrite("compressed.txt","===============\n");
        System.out.println("====================");
    }
    public static String fileWrite(String fileName, String write) {
        String s = "";
        File OS = new File("codebook.txt");
        try {
            FileWriter f = new FileWriter(System.getProperty("user.home") + "/Desktop/" + fileName,true);
            f.write(write);
            f.close();
        } catch (Exception ex) {
        }
        return s;
    }
    public static void main(String[] args) throws IOException {
        image img=new image("cat","png");
        img.readImage();
        int vectorWidth=2;
        int vectorHeight=2;
        vectorQuantization v=new vectorQuantization(vectorWidth,vectorHeight,16,img);
        System.out.println(img.pixels.length);
        System.out.println(img.pixels[0].length);
        v.createVectors();
        ArrayList<int[][]>codebook=new ArrayList<>();
        codebook=v.createCodeBook(v.getVectors(),vectorWidth,vectorHeight,16);
        String[][] compressedImage=v.encode(img.pixels,codebook,vectorHeight,vectorWidth);
        printCompressedImage(compressedImage);
        int[][]reconstructedImage=v.decode(v.getVectors(),codebook,vectorHeight,vectorWidth);
        img.writeImage(reconstructedImage,img.width,img.height,"reconstructed5");
        for(int i=0;i< codebook.size();i++){
            printCodeBook(codebook.get(i));

        }
    }
}