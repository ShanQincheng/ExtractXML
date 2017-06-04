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
