<html>
<#import "/spring.ftl" as spring/>
<#import "../common.ftl" as common/>
<#import "../commonGWT.ftl" as gwt/>
<head>
  <title>My List</title>
  <@gwt.maps/>
</head>




<body id="mylist">					
        
 <#if message?exists>
	<div style="z-index: 99; position: absolute; left: 200px;">
	 <div class="message">${message}</div>
	</div>
 </#if>			  	 	  


  	
  	<div id="main">
  	 
	
	<@common.box "boxStyle", "myList", "MyList">
		<@gwt.widget "MyPage"/>		
		<@gwt.finalize/>	
	</@common.box>	
	
	
	</div><!--end browseWidgets-->  

	
    
</body>
</html>