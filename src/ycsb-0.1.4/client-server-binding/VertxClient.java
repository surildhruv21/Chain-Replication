
import java.io.*;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.net.*;

import org.json.JSONException;
import org.json.JSONObject;

import com.yahoo.ycsb.ByteIterator;
import com.yahoo.ycsb.DB;
import com.yahoo.ycsb.StringByteIterator;


import java.io.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
/*
* @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
*/
public class VertxClient{
// Convenience method so you can run it in your IDE
	public static void main(String[] args) throws IOException{
		
		String value = "{\"glossary\": {\"title%\": \"example glossary\"}}";
		System.out.println(new JSONObject(value));
		//String value = "{\"field1\":\"2%>;5=<\"091\"*(>48&:&(/8#!&63\";#6/'0843=?#-4+2(=%+490(?0 ,?/'+#3)-($599-# \"304?5/<';7\"!'.'.%?626($#%!\",\"field0\":\"',&$\"%\"%\"21!+ 6= <6'1. 3>-*'>%+#85(:.5#2'5&12&//+\"4:)9'-!.$+<5#9:71&=9( 5+.%\");6=7+9$#.8'&)!2)9$%<)&\",\"field7\":\"+5?-! 8(=1'>$?#91 91(9\"85\"553>!5=?)$\"+9.-9')*=8-(:#\";+0>'#;4<\/)#+ 9%2>.?>31+2(919*:5<0*)'3: !&$7$:;#\",\"field6\":\"$23.0%/00<+&1(%;,#<>'2>(*. #4<&.$=*5;.&54: \":0,?7 ?)\";:?$(:7%*..2>5=%776 \"('43/<&>/89)3+5+>,(>/,7.8-\",\"field9\":\".79+++808, 4/*=+$%8*94\">\"5-525<#'6!+&%.9<.;+#55 -4/+.:6.-. $7'<8('>:'+5( 7=.!,=$5338'74/8= 7? =,4%;3\",\"field8\":\"#-:40;%0#)&';<)96;&3:56<#5(/3.#=:;2,1<\"0--?8,!*->-';==7:)(-5.51>)3.,-?5$#.392:%+0*>92,$+$!)25,>-/$1:\",\"field3\":\"+):('&%'5?=$3!70'&>7!-8#1&<198=23$.=)-:!/?6=%+2,  459+,%:(< &*5&;.=\"6\"!<.<25;9)!%\"%2)'5 )4!43.$&=*-;\",\"field2\":\"8>-&)9+ +*!43$3 %?:: &='2;%($+*0'3)9:9!'$,*0+);<(7232)&\":+\"'=&*9%*$1/#!(&1\"'=3(+$$)#8.+/+,23\"*8%42<-\",\"field5\":\":)!4'6&8?:'09;\"\",;2?)9'+* *9+,,23$+92.1+55\")+?>2*:$'2>76$&=506#=6*=+9,)>059. 5$?'6>;,40*7=$*.'79'/97\",\"field4\":\":(8),8)=//%,!'5*8$!,3-\"=?:'#-?\"(?%0#=!!:<>(\"(06!<)# /&0:8=$!<-9<4: *)+(/1,48'6>$649-23'99#!3)+><-. &\"}";
		
		StringBuffer resultant = new StringBuffer();
		try{
			URI uri = new URI( String.format( 
                           "http://localhost:8080/put?"+"key=%s", 
                           URLEncoder.encode( value , "UTF-8" ) ) );
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(uri);
			HttpResponse response = client.execute(request);
			BufferedReader rd = new BufferedReader(
				new InputStreamReader(response.getEntity().getContent()));

			String line = "";
			while ((line = rd.readLine()) != null) {
				resultant.append(line);
			}
		} catch (Exception e){
			e.printStackTrace();
		}

		StringBuilder jsonStr = new StringBuilder();
		JSONObject jsonObject;

		String relative_path = "/get?key";

		String url = "http://localhost:8080"+relative_path;
		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		try{
			HttpResponse response = client.execute(request);
			String response_body = URLDecoder.decode(EntityUtils.toString(response.getEntity(), "UTF-8"));
			System.out.println("response body "+response_body);
			jsonStr.append(response_body);
			System.out.println("jsonStr "+jsonStr);

		} catch (Exception e){
			e.printStackTrace();
		}
		
		int ret = 0;
		if ((jsonStr.toString()).equals(value))
			System.out.println("yaaaay");	

		HashMap<String, ByteIterator> result = new HashMap<String, ByteIterator>();
		try {
			System.out.println("jsonStr "+jsonStr);
			jsonObject = new JSONObject(jsonStr.toString());
			System.out.println("jsonobject "+jsonObject);
		} catch(JSONException e) {
			result = new HashMap<String, ByteIterator>();
			System.err.println(e.getMessage());
		}

	}
}
