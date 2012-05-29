/*
Copyright (c) 2010, Jesper André Lyngesen Pedersen
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

 - Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.

 - Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.signaut.common.couchdb;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
@JsonInclude(Include.NON_NULL)
public class CouchDbUser extends Document {

    private String name;
    @JsonProperty("password_sha")
    private String password;
    private String roles[];
    private String salt;

    public String getName() {
        return name;
    }
    public CouchDbUser setName(String name) {
        this.name = name;
        return this;
    }
    @JsonProperty("password_sha")
    public String getPassword() {
        return password;
    }
    public CouchDbUser setPassword(String password) {
        this.password = password;
        return this;
    }
    public String[] getRoles() {
        if (roles == null) {
            return new String[0]; //Roles must be defined
        }
        return roles;
    }
    public CouchDbUser setRoles(String[] roles) {
        this.roles = roles;
        return this;
    }
    public String getSalt() {
        return salt;
    }
    public CouchDbUser setSalt(String salt) {
        this.salt = salt;
        return this;
    }
    
    public CouchDbUser setPlainTextPassword(String password) {
        this.password = toHex(encode(password));
        return this;
    }
    
    private byte[] encode(String plaintext) {
        if (salt == null) {
            salt = UUID.randomUUID().toString();
        }
        try {
            final MessageDigest digester = MessageDigest.getInstance("SHA-1");
            return digester.digest((plaintext + salt).getBytes());
        } catch (NoSuchAlgorithmException e) {
            throw new CouchDbException("While encoding password for " + name, e);
        } 
    }
    
    private String toHex(byte bytes[]) {
        final StringBuilder hexString = new StringBuilder();
        for (byte b: bytes) {
            final int cleanByte = b & 0xff;
            final String s = Integer.toHexString(cleanByte);
            if (cleanByte < 0x10) {
                hexString.append("0");
            }
            hexString.append(s);
        }
        return hexString.toString();
    }

    public String getType() {
        return "user";
    }
}
