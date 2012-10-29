/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
