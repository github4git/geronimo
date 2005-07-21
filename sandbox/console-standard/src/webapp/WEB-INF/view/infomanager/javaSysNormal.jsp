<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">Java</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>java.awt.graphicsenv</td>
    <td class="LightBackground" width="80%">${javaSysProps['java.awt.graphicsenv']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.awt.printerjob</td>
    <td class="MediumBackground">${javaSysProps['java.awt.printerjob']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.class.path</td>
    <td class="LightBackground">
        <table>
        <c:forEach var="el" items="${javaClassPath}">
            <tr><td class="LightBackground">${el}</td></tr>
        </c:forEach>
        </table>
    </td>
  </tr>
  <tr>
    <td class="MediumBackground">java.class.version</td>
    <td class="MediumBackground">${javaSysProps['java.class.version']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.endorsed.dirs</td>
    <td class="LightBackground">${javaSysProps['java.endorsed.dirs']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.ext.dirs</td>
    <td class="MediumBackground">${javaSysProps['java.ext.dirs']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.home</td>
    <td class="LightBackground">${javaSysProps['java.home']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.io.tmpdir</td>
    <td class="MediumBackground">${javaSysProps['java.io.tmpdir']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.library.path</td>
    <td class="LightBackground">
        <table>
        <c:forEach var="el" items="${javaLibraryPath}">
            <tr><td class="LightBackground">${el}</td></tr>
        </c:forEach>
        </table>
    </td>
  </tr>
  <tr>
    <td class="MediumBackground">java.runtime.name</td>
    <td class="MediumBackground">${javaSysProps['java.runtime.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.runtime.version</td>
    <td class="LightBackground">${javaSysProps['java.runtime.version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.specification.name</td>
    <td class="MediumBackground">${javaSysProps['java.specification.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.specification.vendor</td>
    <td class="LightBackground">${javaSysProps['java.specification.vendor']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.specification.version</td>
    <td class="MediumBackground">${javaSysProps['java.specification.version']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.util.prefs.PreferencesFactory</td>
    <td class="LightBackground">${javaSysProps['java.util.prefs.PreferencesFactory']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vendor-</td>
    <td class="MediumBackground">${javaSysProps['java.vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vendor.url</td>
    <td class="LightBackground">${javaSysProps['java.vendor.url']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vendor.url.bug</td>
    <td class="MediumBackground">${javaSysProps['java.vendor.url.bug']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.version-</td>
    <td class="LightBackground">${javaSysProps['java.version']}</td>
  </tr>
</table>
<br>
<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">Virtual Machine</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>java.vm.info</td>
    <td class="LightBackground" width="80%">${javaSysProps['java.vm.info']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vm.name</td>
    <td class="MediumBackground">${javaSysProps['java.vm.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vm.specification.name</td>
    <td class="LightBackground">${javaSysProps['java.vm.specification.name']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vm.specification.vendor</td>
    <td class="MediumBackground">${javaSysProps['java.vm.specification.vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vm.specification.version</td>
    <td class="LightBackground">${javaSysProps['java.vm.specification.version']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">java.vm.vendor</td>
    <td class="MediumBackground">${javaSysProps['java.vm.vendor']}</td>
  </tr>
  <tr>
    <td class="LightBackground">java.vm.version</td>
    <td class="LightBackground">${javaSysProps['java.vm.version']}</td>
  </tr>
</table>
<br>
<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">Operating System</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>os.arch</td>
    <td class="LightBackground" width="80%">${javaSysProps['os.arch']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">os.name</td>
    <td class="MediumBackground">${javaSysProps['os.name']}</td>
  </tr>
  <tr>
    <td class="LightBackground">os.version</td>
    <td class="LightBackground">${javaSysProps['os.version']}</td>
  </tr>
</table>
<br>
<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">Sun</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>sun.arch.data.model</td>
    <td class="LightBackground" width="80%">${javaSysProps['sun.arch.data.model']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.boot.class.path</td>

    <td class="MediumBackground"><!-- ${javaSysProps['sun.boot.class.path']} -->

        <table>
        <c:forEach var="el" items="${bootClassPathList}">
            <tr><td class="MediumBackground">${el}</td></tr>
        </c:forEach>
        </table>

    </td>
  </tr>
  <tr>
    <td class="LightBackground">sun.boot.library.path</td>
    <td class="LightBackground">${javaSysProps['sun.boot.library.path']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.cpu.endian</td>
    <td class="MediumBackground">${javaSysProps['sun.cpu.endian']}</td>
  </tr>
  <tr>
    <td class="LightBackground">sun.cpu.isalist</td>
    <td class="LightBackground">${javaSysProps['sun.cpu.isalist']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.io.unicode.encoding</td>
    <td class="MediumBackground">${javaSysProps['sun.io.unicode.encoding']}</td>
  </tr>
  <tr>
    <td class="LightBackground">sun.java2d.fontpath</td>
    <td class="LightBackground">${javaSysProps['sun.java2d.fontpath']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">sun.os.patch.level</td>
    <td class="MediumBackground">${javaSysProps['sun.os.patch.level']}</td>
  </tr>
</table>
<br>
<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">User</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>user.country</td>
    <td class="LightBackground" width="80%">${javaSysProps['user.country']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">user.dir</td>
    <td class="MediumBackground">${javaSysProps['user.dir']}</td>
  </tr>
  <tr>
    <td class="LightBackground">user.home</td>
    <td class="LightBackground">${javaSysProps['user.home']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">user.language</td>
    <td class="MediumBackground">${javaSysProps['user.language']}</td>
  </tr>
  <tr>
    <td class="LightBackground">user.name</td>
    <td class="LightBackground">${javaSysProps['user.name']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">user.timezone</td>
    <td class="MediumBackground">${javaSysProps['user.timezone']}</td>
  </tr>
  <tr>
    <td class="LightBackground">user.variant</td>
    <td class="LightBackground">${javaSysProps['user.variant']}</td>
  </tr>
</table>
<br>
<table width="100%">
  <tr>
    <td class="DarkBackground" width="100%" colspan="2" align="center">Etc</td>
  </tr>
  <tr>
    <td class="LightBackground" width="20%" nowrap>path.separator</td>
    <td class="LightBackground" width="80%">${javaSysProps['path.separator']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">line.separator</td>
    <td class="MediumBackground">${javaSysProps['line.separator']}</td>
  </tr>
  <tr>
    <td class="LightBackground">awt.toolkit</td>
    <td class="LightBackground">${javaSysProps['awt.toolkit']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">file.encoding</td>
    <td class="MediumBackground">${javaSysProps['file.encoding']}</td>
  </tr>
  <tr>
    <td class="LightBackground">file.encoding.pkg</td>
    <td class="LightBackground">${javaSysProps['file.encoding.pkg']}</td>
  </tr>
  <tr>
    <td class="MediumBackground">file.separator</td>
    <td class="MediumBackground">${javaSysProps['file.separator']}</td>
  </tr>
</table>