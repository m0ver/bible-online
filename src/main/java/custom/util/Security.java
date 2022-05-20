/*******************************************************************************
 * Copyright  (c) 2013, 2017 James Mover Zhou
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
package custom.util;


import org.apache.commons.codec.digest.DigestUtils;

public class Security {
    private String username = "";

    public Security(String username) {
        this.username = username;
    }

    public String encode(String password) {
        return DigestUtils.md5Hex((password + username).getBytes()).toUpperCase();
    }

    @Deprecated
    public String encodePassword(String password) {
        char[] u = username.toCharArray(), p = password.toCharArray();
        int t = (u[0] - u[0] % 10) / 10;
        for (int i = 0; i < p.length; i++) {
            p[i] += t;
            if (p[i] > 126) p[i] -= 94;
        }
        return String.valueOf(p);
    }

    @Deprecated
    public String decodePassword(String password) {
        char[] u = username.toCharArray(), p = password.toCharArray();
        int t = (u[0] - u[0] % 10) / 10;
        for (int i = 0; i < p.length; i++) {
            p[i] -= t;
            if (p[i] < 33) p[i] += 94;
        }
        return String.valueOf(p);
    }

    public static enum Mode {
        OnlyNumber, NumberAndLetter, ALL
    }

}