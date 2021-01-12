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


import org.tinystruct.ApplicationException;

import java.io.IOException;
import java.util.Base64;


public class ActivationKey {

    private long daysTime;
    private String key;
    private String randomKey;

    public ActivationKey() {
        this.key = "IN-GOD-ASIA";
        this.daysTime = 24 * 3 * 3600 * 1000;
    }

    public String encode(String code) {
        return new String(Base64.getEncoder().encode(code.getBytes()));
    }

    public String decode(String code) throws IOException {
        return new String(Base64.getDecoder().decode(code));
    }

    public String getRandomCode() {
        long current = System.currentTimeMillis();
        long expire = current + this.daysTime;

        this.randomKey = this.encode(key + String.valueOf(expire));

        return this.randomKey;
    }

    public boolean expired(String code) throws ApplicationException {
        long time = 0L;
        boolean expire = true;
        try {
            String millseconds = this.decode(code);
            time = Long.valueOf(millseconds.replaceAll(this.key, ""));
        } catch (IOException e) {

            throw new ApplicationException(e.getMessage(), e.getCause());
        } finally {
            expire = time < System.currentTimeMillis();
        }

        return expire;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public String getRandomKey() {
        return randomKey;
    }

}

