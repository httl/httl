<html>
<body>
{$user.name}/{$user.role}<br/>
{if $user.role eq "admin"}<table>
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
  {foreach from=$books item="book" name="booklist"}{if $book.price gt 0}<tr>
    <td>{math equation="index + 1" format="%b" index=$smarty.foreach.booklist.index}</td>
    <td>{$book.title}</td>
    <td>{$book.author}</td>
    <td>{$book.publisher}</td>
    <td>{$book.publication|date_format:'%Y-%m-%d %T'}</td>
    <td>{$book.price}</td>
    <td>{$book.discount}%</td>
    <td>{math equation="price * discount / 100" format="%d" price=$book.price discount=$book.discount}</td>
  </tr>{/if}{/foreach}
</table>
{elseif $user}<table>
  <tr>
    <td>No privilege.</td>
  </tr>
</table>
{else}<table>
  <tr>
    <td>No login.</td>
  </tr>
</table>{/if}
</body>
</html>