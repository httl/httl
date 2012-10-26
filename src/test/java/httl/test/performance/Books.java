/*
 * Copyright 2011 Alibaba.com All right reserved. This software is the
 * confidential and proprietary information of Alibaba.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Alibaba.com.
 */
package httl.test.performance;

import httl.test.model.Book;
import httl.test.model.User;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.Map;

/**
 * list
 * 
 * @author william.liangf
 */
public class Books {
    
    public void render(Map<String, Object> context, Writer writer) throws IOException {
        User user = (User)context.get("user");
        Book[] books = (Book[]) context.get("books");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        writer.write("<html>\r\n<body>\r\n");
        writer.write(user.getName());
        writer.write("/");
        writer.write(user.getRole());
        writer.write("<br/>\r\n");
        if (user.getRole().equals("admin")) {
        writer.write("<table>\r\n  <tr>\r\n    <th>NO.</th>\r\n    <th>Title</th>\r\n    <th>Author</th>\r\n    <th>Publisher</th>\r\n    <th>PublicationDate</th>\r\n    <th>Price</th>\r\n    <th>DiscountPercent</th>\r\n    <th>DiscountPrice</th>\r\n  </tr>\r\n  ");
        int count = 1;
        for (Book book : books) {
        if (book.getPrice() > 0) {
        writer.write("<tr>\r\n    <td>");
        writer.write(String.valueOf(count ++));
        writer.write("</td>\r\n    <td>");
        writer.write(book.getTitle());
        writer.write("</td>\r\n    <td>");
        writer.write(book.getAuthor());
        writer.write("</td>\r\n    <td>");
        writer.write(book.getPublisher());
        writer.write("</td>\r\n    <td>");
        writer.write(format.format(book.getPublication()));
        writer.write("</td>\r\n    <td>");
        writer.write(String.valueOf(book.getPrice()));
        writer.write("</td>\r\n    <td>");
        writer.write(String.valueOf(book.getDiscount()));
        writer.write("%</td>\r\n    <td>");
        writer.write(String.valueOf(book.getPrice() * book.getDiscount() / 100));
        writer.write("</td>\r\n  </tr>");
        }
        }
        writer.write("\r\n</table>\r\n");
        }
        writer.write("</body>\r\n</html>");
    }
    
}
