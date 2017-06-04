# 前言
* 有一批 xml 格式存儲的天氣情況資料檔
* 現在需要提取出其中指定資料
  * 地理位置
  * 記錄時間
  * 溫度  
* 轉為字符串存放到 .txt 檔案中

###### 檔案資料型別如下
![](/content/images/2017/06/QQ--20170604141526.png)
###### 那麼提取出的一行字符串會是
```
C0C4802016-05-16T12:40:00+08:0020.3
```
###### 代表
桃園地區， 2016-05-16 日 12:40 分，溫度 20.3 ℃


#### 本文不介紹 Dom4j ， Apache maven 的基礎知識
***

# 環境
    Platform                   Windows 10, x64
    IDE                        IntelliJ IDEA 172.2465.6
    Language                   Java
    Jdk                        java version "1.8.0_131"
    Maven                      Apache Maven 3.5.0

***

# 開始
*  分析 xml 資料檔格式
*  使用 dom4j 庫編寫提取程式碼
*  最終效果，以及提供完整 xml 文件下載地址

***
### 分析 xml 資料檔格式
以下為一個 xml 資料檔中的==桃園地區==的完整資料。（ *單個完整的 xml 資料檔存放了很多地區的氣象資料，資料很長很長。為了方便閱讀，只貼出==桃园地区==氣象資料用於理解。*） 

**我們的目的是，將 xml 檔中高亮的的 3 行資料從 xml 資料檔中提取出來，拼接成一個字符串，存儲到 .txt 格式的資料檔中**  
**i.e.**
```
C0C4802016-05-16T12:40:00+08:0020.3
```

```language-python line-numbers data-line=14,16,39
<?xml version="1.0" encoding="UTF-8"?><cwbopendata xmlns="urn:cwb:gov:tw:cwbcommon:0.1">
 <identifier>714af316-3f70-4f70-8822-c79f03969a28</identifier>
 <sender>weather@cwb.gov.tw</sender>
 <sent>2016-05-16T12:50:47+08:00</sent>
 <status>Actual</status>
 <msgType>Issue</msgType>
 <dataid>CWB_A0001</dataid>
 <scope>Public</scope>
 <dataset/>
 <location>
  <lat>24.9943</lat>
  <lon>121.3150</lon>
  <locationName>桃園</locationName>
  <stationId>C0C480</stationId>
  <time>
   <obsTime>2016-05-16T12:40:00+08:00</obsTime>
  </time>
  <weatherElement>
   <elementName>ELEV</elementName>
   <elementValue>
    <value>105.0</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>WDIR</elementName>
   <elementValue>
    <value>0.0</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>WDSD</elementName>
   <elementValue>
    <value>0.2</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>TEMP</elementName>
   <elementValue>
    <value>20.3</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>HUMD</elementName>
   <elementValue>
    <value>0.93</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>PRES</elementName>
   <elementValue>
    <value>1002.5</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>SUN</elementName>
   <elementValue>
    <value>-99.0</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>H_24R</elementName>
   <elementValue>
    <value>-97.0</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>WS15M</elementName>
   <elementValue>
    <value>1.7</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>WD15M</elementName>
   <elementValue>
    <value>326</value>
   </elementValue>
  </weatherElement>
  <weatherElement>
   <elementName>WS15T</elementName>
   <elementValue>
    <value>1233</value>
   </elementValue>
  </weatherElement>
  <parameter>
   <parameterName>CITY</parameterName>
   <parameterValue>桃園市</parameterValue>
  </parameter>
  <parameter>
   <parameterName>CITY_SN</parameterName>
   <parameterValue>08</parameterValue>
  </parameter>
  <parameter>
   <parameterName>TOWN</parameterName>
   <parameterValue>桃園區</parameterValue>
  </parameter>
  <parameter>
   <parameterName>TOWN_SN</parameterName>
   <parameterValue>042</parameterValue>
  </parameter>
 </location>
</cwbopendata>


```

通過分析以上 xml 資料檔格式可知

* 該資料檔的頭尾標籤為`<cwbopendata>（line 1） </cwbopendata>（line 101）`

* 桃園地區的所有氣象信息，存放在 `<location> (line 10)</location> (line 100)`標籤中

* 觀測站 ID 存放在`<location><stationID></stationID></location>`

* 時間信息存放在`<location><time><obsTime></obsTime></location> `

* 氣溫信息存放在`<location><whetherElement><elementValue><value></value></elementValue></whetherElement></location>`

對資料檔格式稍加觀察，就能看出我們想要提取出的資料的在 xml 資料檔中的層級關係了。

***

### 使用 dom4j 庫編寫提取程式碼
IDEA 新建一個 Maven 項目

**pom.xml 添加以下依賴**

```
 <dependencies>
    <dependency>
      <groupId>dom4j</groupId>
      <artifactId>dom4j</artifactId>
      <version>1.6.1</version>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
    </dependency>
  </dependencies>
```

###### 程式碼中幾個重點需要掌握
打開存儲所有 xml 文件的資料夾，將資料夾中的所有 xml 文件放到一個 file array 中。之後的程式會使用這個 array 迭代訪問並解析所有該資料夾中的 xml 文件

```language-python line-numbers
File rootDirectory = new File( "D:\\Develop\\Andorid\\IdeaProjects\\IdeaMaven\\ExtractXML\\src\\resources\\summer" ); // the directory contanis all the xml files
File[] files = rootDirectory.listFiles( ); // put all xml files into a File array
```
將一個 xml 文件的所有資料存放到一個 Document 中。之後所有的資料提取都是從這個 Document 中提取
```language-python line-numbers
Document document = reader.read( file ); // put all xml file content into a Document
```
獲取頭尾標籤`<cwbopendata></cwbopendata>`，之後迭代訪問該標籤中的所有元素
```language-python line-numbers
Element root = document.getRootElement( ); //  get the root element, i.e. <cwbopendata></cwbopendata>
Iterator iterator = root.elementIterator( ); // iterator read the contents between <cwbopendata> and </cwbopendata>
```
獲取某個標籤中的字符串值，  
**i.e. root.getName.equal("cwbopendata") = = 1**
```
Element.getName( );
```
獲取某個標籤下的子標籤中存放的值。  
**i.e. root.element("locationName").getStringValue().equal("桃園") = = 1**
```
Element.element( "elementName" ).getStringValue( )
```

##### 具體程式碼
```language-python line-numbers
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Test;

/**
 * Created by codingstory on 5/26/2017.
 */
public class ExtractXML {

  // read data from xml file
  @Test
  public void readText( ) throws Exception {

    File rootDirectory = new File(  // the directory contanis all the xml files
        "D:\\Develop\\Andorid\\IdeaProjects\\IdeaMaven\\ExtractXML\\src\\resources\\summer" );
    File[] files = rootDirectory.listFiles( ); // put all xml files into a File array
    SAXReader reader = new SAXReader( ); // get parser object
    File oneDayFile = new File( "summer.txt" ); // create a txt file to store the extract strings
    //BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter( oneDayFile, true ) );
    BufferedWriter bufferedWriter = new BufferedWriter( new FileWriter( oneDayFile ) ); // create a bufferedWriter to store all extracted temperature information in one xml file
    try {
      for ( File file : files ) { // iterator all xml file in the directory

        String filename = file.getName( ).substring( 0, file.getName( ).lastIndexOf( "." ) );  // delete the file extension
        ;
        System.out.println( filename.substring( 14, 19 ) );  // printf file name without extension for log
        String fileCode = filename.substring( 14, 19 ).trim( ); // extract the file code from file name
        if ( fileCode.equals( "A0002" ) || fileCode.equals( "A0004" ) || file.length( ) == 0 ) {  // we don't need the xml file contains "A0002" and "A0004" string in file name, just skip them
          continue;
        }

        Document document = reader.read( file ); // put all xml file content into a Document
    /*
    // get document object according to parser object
    Document document = reader.read( new File(
        "D:\\Develop\\Andorid\\IdeaProjects\\IdeaMaven\\ExtractXML\\src\\resources\\book4.xml" ) );
        */
        // get root node (here is location)
        Element root = document.getRootElement( ); //  get the root element, i.e. <cwbopendata></cwbopendata>
        // get iterator
        Iterator iterator = root.elementIterator( ); // iterator read the contents between <cwbopendata> and </cwbopendata>
        StringBuffer stringBuffer = new StringBuffer( );  // crate a string buffer to build one location temperature information

        boolean tempratureFlag = false;   // determine whether current is <ElementName> TEMP </ElementName>, i.e. current Temperature information is stored in the <ElementValue> between current <ElementName>
        boolean hasTemperatureFlag = false;  // some xml file lost the temperature information, we should judge these file and skip them

        // Traverse the elements between  the  <cwbopendata> and   </cwbopendata>
        while ( iterator.hasNext( ) ) {
          Element currentLocation = ( Element ) iterator.next( ); // get current elements content
          //  System.out.println("ElementName: " + currentLocation.getName() + "   Value: " +  currentLocation.getStringValue() );
          if ( currentLocation.getName( ).equals( "location" ) ) {  // if current element is <location> , enter the element

            Iterator locationIterator = currentLocation.elementIterator( );  // create a Iterator to iterator all the element between <location> and </location>
            while ( locationIterator.hasNext( ) ) {
              Element locationAttributes = ( Element ) locationIterator.next( ); // get current elements content
              if ( locationAttributes.getName( ).equals( "stationId" ) ) {  // if current element is <stationId>, we need to store the stationId value between <stationID> and </stationID>
                System.out.print( "StationID:  " + locationAttributes.getStringValue( ) + "  " );
                stringBuffer.append( locationAttributes.getStringValue( ) ); // append "stationID value" to the stringBuffer
              } else if ( locationAttributes.getName( ).equals( "time" ) ) { // if current element is <time>, we need to store the obsTime value between <time><obsTime> and </obsTime></time>
                System.out.println(
                    "CurrentTime: " + locationAttributes.element( "obsTime" ).getStringValue( ) );
                stringBuffer.append( locationAttributes.element( "obsTime" ).getStringValue( ) ); // append "obsTime value" to the stringBuffer

              } else if ( locationAttributes.getName( ).equals( "weatherElement" ) ) {  // if current element is <weatherElement> , enter the element
                Iterator whetherIterator = locationAttributes.elementIterator( );  // create a Iterator to iterator all the element between <weatherElement> and </weatherElement>
                while ( whetherIterator.hasNext( ) ) {
                  Element whetherElement = ( Element ) whetherIterator.next( );
                  if ( whetherElement.getName( ).equals( "elementName" ) ) { // if current element is <elementName>
                    System.out.print( whetherElement.getStringValue( ) + "  " );
                    if ( whetherElement.getStringValue( ).equals( "TEMP" ) ) {  // and the value of current elementName is "TEMP"
                      tempratureFlag = true;                  // mark current <elementName> value is "TEMP", we need to store the temperature value in <elementValue> now
                      hasTemperatureFlag = true;             // mark this file contains <TEMP> value
                    }
                  } else if ( whetherElement.getName( ).equals( "elementValue" ) ) {
                    //System.out.println( whetherElement.getStringValue());
                    System.out.println( whetherElement.element( "value" ).getText( ) );
                    if ( tempratureFlag ) {                  // if current elementValue is between <elementName>TEMP</elementName>
                      stringBuffer.append( whetherElement.element( "value" ).getText( ) );  // append the temperature value to stringBuffer
                      tempratureFlag = false;
                    }
                  }
                }
              }
            }
            if ( !hasTemperatureFlag ) {       // if current file doesn't contains temperature value, break and read next xml file
              System.out.println( "This file doesn't contain temperature data" );
              break;
            }

            System.out.println( stringBuffer.toString( ) );
            //stringBuffer.append( System.getProperty( "line.separator" ) );
            String obsTime = stringBuffer.toString( );
            if(obsTime.length() == 0 || obsTime.length() < 32){
              continue;
            }
            obsTime.concat( "0" );  // the operation string to float will lose accuracy, so we can avoid lose accuracy according to append one character than parse string to float
            Float temperature = Float.valueOf( obsTime.substring( 31, obsTime.length( ) ).trim( ) );
            //System.out.println( currentTimeString );
            System.out.println( temperature );

            /*
            Date currentTimeDate = new Date( );
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat( "HH:mm:ss" );
            String currentTimeString = obsTime.substring( 17, 24 );
            currentTimeDate = simpleDateFormat.parse( currentTimeString );
            if ( currentTimeDate.after( simpleDateFormat.parse( "17:00:00" ) ) && currentTimeDate
                .before( simpleDateFormat.parse( "23:59:59" ) ) ) {
              System.out.println( "yes!!!!!!!!!!yes!!!!!!!!!yes!!!!!!!!!" );
            }
            */
            bufferedWriter.write( stringBuffer.toString( ) );  // write one location temperature information string to .txt file
            bufferedWriter.write( System.getProperty( "line.separator" ) );  // write string information to .txt file line by line
            //    bufferedWriter.flush();
            stringBuffer.delete( 0, stringBuffer.length( ) );  // clean the stringBuffer
            bufferedWriter.flush( );  // commit and write to the .txt file actually

          }

        }

      }
    } catch ( IOException e ) {
      e.printStackTrace( );
    } finally {
      try {
        // close the writer regardless of what happens ...
        bufferedWriter.close( );
      } catch ( Exception e ) {
      }
    }
  }
}

```

***

### 最終效果，以及提供完整 xml 文件下載地址
![](/content/images/2017/06/QQ--20170604162053.png)

***
# 以上


2017 年 6 月 4 日