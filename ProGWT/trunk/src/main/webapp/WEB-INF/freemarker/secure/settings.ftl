<html>
<#import "/spring.ftl" as spring/>
<#import "../common.ftl" as common/>
<head>
  <title>My Settings</title>  
</head>

<body id="mySettings">					
        
 <#if message?exists>
	<div style="z-index: 99; position: absolute; left: 200px;">
	 <div class="message">${message}</div>
	</div>
 </#if>			  	 	  

  	
  	<div id="main">
  	 
	
	<@common.box "boxStyle", "myList", "Settings">
	
	   <form action="<@spring.url "/site/secure/settings.html"/>" method="POST">
        <fieldset>          
         <p>
             <label for="oldPassword"><@spring.formPasswordInput "command.oldPassword"/><@common.regError/>Old Password 
             </label>
         <p>
             <label for="newPassword"><@spring.formPasswordInput "command.newPassword"/><@common.regError/>New Password
             </label>
         
         <p>
         <input name="login" value="Change" type="submit">
        </fieldset>
     </form>     
			
	</@common.box>	
	
	
	</div><!--end browseWidgets-->  

	
    
</body>
</html>