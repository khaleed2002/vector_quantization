import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.Math;
import javax.imageio.ImageIO;
class image {
    private String imageName;
    private String imageType;
    private File imageFile;
    public int pixels[][];
    private BufferedImage bufferedImage;
    public int width=0;
    public int height=0;
    public image(String imageName,String imageType) throws IOException {
        this.imageName=imageName;
        this.imageType=imageType;
        imageFile=new File(System.getProperty("user.home")+"/Desktop/"+imageName+"."+imageType);
        bufferedImage=ImageIO.read(imageFile);
        if(bufferedImage.getWidth()%2==0)
            width=bufferedImage.getWidth();
        else
            width=bufferedImage.getWidth()+1;
        if(bufferedImage.getHeight()%2==0)
            height=bufferedImage.getHeight();
        else
            height=bufferedImage.getHeight()+1;

    }
    public void readImage()
    {

        pixels=new int[height][width];

        for(int x=0;x<bufferedImage.getWidth();x++)
        {
            for(int y=0;y<bufferedImage.getHeight();y++)
            {
                int rgb=bufferedImage.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                pixels[y][x]=r;
            }
        }
        if(height> bufferedImage.getHeight()){
            for(int i=0;i<width;i++){
                pixels[height-1][i]=pixels[height-2][i];//complete row
            }
        }
        if(width> bufferedImage.getWidth()){
            for(int i=0;i<height;i++){
                pixels[i][width-1]=pixels[i][width-2];//complete column
            }
        }

    }
    public void writeImage(int[][] pixels,int width,int height,String outPut) {
        File output = new File(System.getProperty("user.home")+"/Desktop/"+outPut+"."+imageType);
        BufferedImage bufferedImage=new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bufferedImage.setRGB(x, y, (pixels[y][x] << 16) | (pixels[y][x] << 8) | (pixels[y][x]));
            }
        }
        try {
            ImageIO.write(bufferedImage, imageType, output);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
class vectorQuantization{
    private image img;
    private int vectorWidth=0;
    private int vectorHeight=0;
    private int codeBookSize=0;
    private ArrayList<int[][]>vectors;
    public ArrayList<ArrayList<int[][]>>nearest=new ArrayList<>();
    public vectorQuantization(int vectorWidth,int vectorHeight,int codeBookSize,image img){
        this.vectorHeight=vectorHeight;
        this.vectorWidth=vectorWidth;
        this.codeBookSize=codeBookSize;
        this.img=img;
        this.img.readImage();
    }

    public ArrayList<int[][]> getVectors() {
        return vectors;
    }

    public void setVectorHeight(int vectorHeight) {
        this.vectorHeight = vectorHeight;
    }

    public void setVectorWidth(int vectorWidth) {
        this.vectorWidth = vectorWidth;
    }

    public void setCodeBookSize(int codeBookSize) {
        this.codeBookSize = codeBookSize;
    }

    public void setImg(image img) {
        this.img = img;
    }
    public void createVectors(){
        vectors=new ArrayList<int[][]>();
        for(int k=0;k<img.width;k+=vectorWidth){
            for(int u=0;u<img.height;u+=vectorHeight){
                vectors.add(getVector(u,k));
            }
        }
    }
    public int[][] getVector(int start_row,int start_column){
        int[][]tmp=new int[vectorHeight][vectorWidth];
        for (int i = 0; i < vectorWidth; i++) {
            for (int j = 0; j < vectorHeight ; j++) {
                tmp[j][i]=img.pixels[start_row + j][start_column + i];
            }
        }
        return tmp;
    }
    public int[][]collectVectors(ArrayList<int[][]>vectors,int vectorWidth,int vectorHeight,int width,int height) {
        int[][] pixels = new int[height][width];
        int row=0,column=0;
        for(int k=0;k<vectors.size();k++){
            int[][] tmp=vectors.get(k);
            for (int i = 0; i < vectorWidth; i++) {
                for (int j = 0; j < vectorHeight ; j++) {
                    pixels[row + j][column + i]=tmp[j][i];
                }
            }
            if(row<height-vectorHeight){
                row+=vectorHeight;
            }
            else{
                row=0;
                if(column<width-vectorWidth)
                column+=vectorWidth;
                else
                    break;
            }
        }
        return pixels;
    }
    public float[][]getAverage(ArrayList<int[][]>vectors,int vectorHeight,int vectorWidth){
        float[][]average=new float[vectorHeight][vectorWidth];
        for (int i = 0; i < vectorWidth; i++) {
            for (int j = 0; j < vectorHeight ; j++) {
                average[j][i]=0;
            }
        }
        for(int t=0;t<vectors.size();t++) {
            int[][] tmp=vectors.get(t);
            for (int i = 0; i < vectorWidth; i++) {
                for (int j = 0; j < vectorHeight; j++) {
                    average[j][i] += tmp[j][i];
                }
            }
        }
        for (int i = 0; i < vectorWidth; i++) {
            for (int j = 0; j < vectorHeight; j++) {
                average[j][i] = average[j][i]/(vectors.size());
            }
        }
     return average;
    }
    public ArrayList<int[][]>split(float[][]average,int vectorWidth,int vectorHeight){
        ArrayList<int[][]>rounds=new ArrayList<int[][]>();
        int[][]small = new int[vectorHeight][vectorWidth];
        int[][]big=new int[vectorHeight][vectorWidth];
        for (int i = 0; i < vectorWidth; i++) {
            for (int j = 0; j < vectorHeight; j++) {
                small[j][i]=(int)average[j][i];
            }
        }
        for (int i = 0; i < vectorWidth; i++) {
            for (int j = 0; j < vectorHeight; j++) {
                float n=average[j][i]+1;
                big[j][i]=(int)n;
            }
        }
        rounds.add(small);
        rounds.add(big);
        return rounds;
    }
    private int getNearestVectorIndex(ArrayList<int[][]>rounds,int[][]vector,int vectorHeight,int vectorWidth){
        int[]differences=new int[rounds.size()];
        for(int t=0;t<rounds.size();t++){
            int[][]tmp=rounds.get(t);
            for (int i = 0; i < vectorWidth; i++) {
                for (int j = 0; j < vectorHeight; j++) {
                    differences[t]+=Math.abs(tmp[j][i]-vector[j][i]);
                }
            }
        }
        int min = 0;
        for (int i = 1; i < differences.length; i++) {
            if (differences[i] < differences[min]) {
                min = i;
            }
        }
        return min;
    }
    static int highestPowerOf2(int input) {
        int nextPowerOf2 = 2;
        while (nextPowerOf2 < input) {
            nextPowerOf2 = nextPowerOf2 * 2;
        }
        return nextPowerOf2;
    }
        public ArrayList<int[][]>getNearestVectorsFor1codeBook(ArrayList<int[][]>vectors,ArrayList<int[][]>averageVectors,int vectorHeight,int vectorWidth,int index){
            ArrayList<int[][]>nearest=new ArrayList<>();
            for(int i=0;i<vectors.size();i++){
                if(getNearestVectorIndex(averageVectors,vectors.get(i),vectorHeight,vectorWidth)==index){
                    nearest.add(vectors.get(i));
                }
            }
            return nearest;
        }
    public ArrayList<int[][]>createCodeBook(ArrayList<int[][]>vectors,int vectorWidth,int vectorHeight,int codeBookSize){
        float[][]average=getAverage(vectors,vectorHeight,vectorWidth);
        ArrayList<int[][]>codeBook;
        ArrayList<int[][]>vectorsForSplitting=split(average,vectorWidth,vectorHeight);
        while(true){
            //for get the nearest vectors
            for(int i=0;i<vectorsForSplitting.size();i++){
                ArrayList<int[][]>near;
                near=getNearestVectorsFor1codeBook(vectors,vectorsForSplitting,vectorHeight,vectorWidth,i);
                nearest.add(near);
            }
            if(vectorsForSplitting.size()>=codeBookSize){
                break;
            }
            else {
                ArrayList<float[][]>averages=new ArrayList<>();
                for(int i=0;i<vectorsForSplitting.size();i++){
                    averages.add(getAverage(nearest.get(i),vectorHeight,vectorWidth));
                }
                vectorsForSplitting.clear();
                for(int i=0;i< averages.size();i++){
                    ArrayList<int[][]>splits=split(averages.get(i),vectorWidth,vectorHeight);
                    vectorsForSplitting.add(splits.get(0));
                    vectorsForSplitting.add(splits.get(1));
                }
            }

        }
        codeBook=vectorsForSplitting;
        return codeBook;
    }
    public String[]getBinary(int codeBookSize){
        String[]binary=new String[codeBookSize];
        for(int i=0;i<codeBookSize;i++){
            String bin=Integer.toBinaryString(i);
            String s="";
            int n=(int)(Math.log(highestPowerOf2(codeBookSize)) / Math.log(2));
            n=n-bin.length();
            for(int j=0;j<n;j++){
                s=s+"0";
            }
            bin=s+bin;
            binary[i]=bin;
        }
        return binary;
    }
    public String[][] encode(int[][]vectors,ArrayList<int[][]>codeBook,int vectorHeight,int vectorWidth){
        String[][]newVec=new String[img.height/vectorHeight][img.width/vectorWidth];
        String[]Binary=getBinary(codeBookSize);
        String tmp;
        for (int i = 0; i < img.width/vectorWidth; i++) {
            for (int j = 0; j < img.height / vectorHeight; j++) {
                tmp = Binary[getNearestVectorIndex(codeBook, getVectors().get(i), vectorHeight, vectorWidth)];
                newVec[j][i] = tmp;
            }
        }
        return newVec;
    }
    public int[][]decode(ArrayList<int[][]>vectors,ArrayList<int[][]>codeBook,int vectorHeight,int vectorWidth){
        ArrayList<int[][]>tmp=new ArrayList<>();
        int index;
        for(int i=0;i<vectors.size();i++){
            index=getNearestVectorIndex(codeBook,vectors.get(i),vectorHeight,vectorWidth);
            tmp.add(codeBook.get(index));
        }
        int[][]g=collectVectors(tmp,vectorWidth,vectorHeight, img.width, img.height);
        return g;
    }
}

