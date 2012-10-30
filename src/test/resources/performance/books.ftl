<html>
<body>
${user.name}/${user.role}<br/>
<#if user.role == "admin">
<table>
  <tr>
    <th>NO.</th>
    <th>Title</th>
    <th>Author</th>
    <th>Publisher</th>
    <th>PublicationDate</th>
    <th>Price</th>
    <th>DiscountPercent</th>
    <th>DiscountPrice</th>
  </tr>
  <#list books as book>
  <#if book.price &gt; 0>
  <tr>
    <td>${book_index + 1}</td>
    <td>${book.title}</td>
    <td>${book.author}</td>
    <td>${book.publisher}</td>
    <td>${book.publication?string("yyyy-MM-dd HH:mm:ss")}</td>
    <td>${book.price}</td>
    <td>${book.discount}%</td>
    <td><#assign discountPrice = book.price * book.discount / 100>${discountPrice?string("0")}</td>
  </tr>
  </#if>
  </#list>
</table>
<#elseif user>
<table>
  <tr>
    <td>No privilege.</td>
  </tr>
</table>
<#else>
<table>
  <tr>
    <td>No login.</td>
  </tr>
</table>
</#if>
</body>
</html>