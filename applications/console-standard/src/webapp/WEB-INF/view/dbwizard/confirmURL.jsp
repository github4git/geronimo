<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Create Database Pool</b> -- Step 3: Final Pool Configuration</p>

<script language="JavaScript">
function <portlet:namespace/>validate() {

   if (document.<portlet:namespace/>DatabaseForm.minSize.value == "") {
      document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
   }
   if (document.<portlet:namespace/>DatabaseForm.maxSize.value == "") {
      document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
   }

   var min = parseInt(document.<portlet:namespace/>DatabaseForm.minSize.value); 
   var max = parseInt(document.<portlet:namespace/>DatabaseForm.maxSize.value); 
   result = true;

   if (isNaN(min)) {
      alert("Min pool size must be a number. Defaulted to 0");
      min = document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
      result = false;
   }
   if (min < 0)
   {
      alert("Min pool size must be non-negative. Defaulted to 0");
      min = document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
      result = false;
   }

   if (isNaN(max)) {
      alert("Max pool size must be a number. Defaulted to 10");
      max = document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
      result = false;
   }
   if (max <= 0)
   {
      alert("Max pool size must be greater than zero. Defaulted to 10" );
      max = document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
      result = false;
   } 

   if (min > max) {
      alert("Max pool size must be greater than Min pool size." );
      return false;
   }

   return result;
}
</script>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-url" />
    <input type="hidden" name="test" value="true" />
    <input type="hidden" name="name" value="${pool.name}" />
    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="user" value="${pool.user}" />
    <input type="hidden" name="password" value="${pool.password}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="driverClass" value="${pool.driverClass}" />
    <input type="hidden" name="jar1" value="${pool.jar1}" />
    <input type="hidden" name="jar2" value="${pool.jar2}" />
    <input type="hidden" name="jar3" value="${pool.jar3}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <table border="0">
    <!-- ENTRY FIELD: URL -->
      <tr>
        <th style="min-width: 140px"><div align="right">JDBC Connect URL:</div></th>
        <td><input name="url" type="text" size="50" value="${pool.url}"></td>
      </tr>
      <tr>
        <td></td>
        <td>Make sure the generated URL fits the syntax for your JDBC driver.</td>
      </tr>
    <!-- STATUS FIELD: Driver Load -->
      <tr>
        <th><div align="right">Driver Status:</div></th>
        <td><i>Loaded Successfully</i></td>
      </tr>
    <!-- HEADER -->
      <tr>
        <th colspan="2">Connection Pool Parameters</th>
      </tr>
    <!-- ENTRY FIELD: Min Size -->
      <tr>
        <th><div align="right">Pool Min Size:</div></th>
        <td><input name="minSize" type="text" size="5" value="${pool.minSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The minimum number of connections in the pool.  Leave blank for default.</td>
      </tr>
    <!-- ENTRY FIELD: Max Size -->
      <tr>
        <th><div align="right">Pool Max Size:</div></th>
        <td><input name="maxSize" type="text" size="5" value="${pool.maxSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The maximum number of connections in the pool.  Leave blank for default.</td>
      </tr>
    <!-- ENTRY FIELD: Blocking Timeout -->
      <tr>
        <th><div align="right">Blocking Timeout:</div></th>
        <td><input name="blockingTimeout" type="text" size="7" value="${pool.blockingTimeout}"> (in milliseconds)</td>
      </tr>
      <tr>
        <td></td>
        <td>The length of time a caller will wait for a connection.  Leave blank for default.</td>
      </tr>
    <!-- ENTRY FIELD: Idle timeout -->
      <tr>
        <th><div align="right">Idle Timeout:</div></th>
        <td><input name="idleTimeout" type="text" size="5" value="${pool.idleTimeout}"> (in minutes)</td>
      </tr>
      <tr>
        <td></td>
        <td>How long a connection can be idle before being closed.  Leave blank for default.</td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
          <input type="button" value="Test Connection" onclick="if (<portlet:namespace/>validate()){document.<portlet:namespace/>DatabaseForm.test.value='true';document.<portlet:namespace/>DatabaseForm.submit();}" />
          <input type="button" value="Skip Test and Deploy" onclick="if (<portlet:namespace/>validate()){document.<portlet:namespace/>DatabaseForm.test.value='false';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
          <input type="button" value="Skip Test and Show Plan" onclick="if (<portlet:namespace/>validate()){document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
