/*******************************************************************************
 * Copyright  (c) 2013 Mover Zhou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package custom.application;

import custom.objects.keyword;
import org.tinystruct.AbstractApplication;
import org.tinystruct.ApplicationException;
import org.tinystruct.data.component.Row;
import org.tinystruct.data.component.Table;
import org.tinystruct.http.Method;
import org.tinystruct.http.Request;
import org.tinystruct.system.annotation.Action;
import org.tinystruct.system.util.StringUtilities;

import java.io.UnsupportedEncodingException;

import static org.tinystruct.http.Constants.HTTP_REQUEST;

public class keywords extends AbstractApplication {

    private Request request;

    @Override
    public void init() {
        // TODO Auto-generated method stub
    }

    @Override
    public String version() {
        // TODO Auto-generated method stub
        return null;
    }

    @Action("bible/keywords")
    public String list() throws ApplicationException {
        String keyword;
        this.request = (Request) this.context.getAttribute(HTTP_REQUEST);

        if (this.request.getParameter("keyword") != null && !this.request.getParameter("keyword").equals("") && new StringUtilities(this.request.getParameter("keyword").toString()).safe()) {
            if (this.request.method() == Method.GET) {
                keyword = this.request.getParameter("keyword");
                try {
                    keyword = new String(keyword.getBytes("ISO8859-1"), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            } else
                keyword = this.request.getParameter("keyword");
        } else {
            keyword = null;
        }

        assert keyword != null;
        keyword = keyword.replaceAll("[%_]", "");

        StringBuilder k = new StringBuilder();
        StringBuilder v = new StringBuilder();
        String k_item;
        String v_item;

        String javascript_block;
        String keywordlist, keywordvisit;

        if (!keyword.isEmpty()) {
            keyword _keyword = new keyword();
            Table findTable = _keyword.findWith(
                    "WHERE keyword LIKE ? ORDER BY visit LIMIT 0,7",
                    new Object[]{"%" + keyword + "%"});

            Row currentRow;
            for (Row fields : findTable) {
                currentRow = fields;
                k_item = currentRow.getFieldInfo("keyword").stringValue();
                if (!k.toString().trim().isEmpty()) {
                    k.append(",\"").append(k_item).append("\"");
                } else {
                    k = new StringBuilder("\"" + k_item + "\"");
                }

                v_item = currentRow.getFieldInfo("visit").stringValue();
                if (!v.toString().trim().isEmpty()) {
                    v.append(",\"").append(v_item).append("\"");
                } else {
                    v = new StringBuilder("\"" + v_item + "\"");
                }
            }
        }

        keywordlist = "new Array(";
        keywordlist += k;
        keywordlist += ")";

        keywordvisit = "new Array(";
        keywordvisit += v;
        keywordvisit += ")";
        javascript_block = "window.listener.sendRPCDone(frameElement,\""
                + keyword + "\"," + keywordlist + "," + keywordvisit
                + ",new Array(\"\"));";

        return javascript_block;
    }

}