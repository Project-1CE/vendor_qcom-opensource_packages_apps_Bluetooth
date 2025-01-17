/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.bluetooth.mapclient;

import com.android.bluetooth.mapclient.MasClient.CharsetType;
import com.android.internal.annotations.VisibleForTesting;

import java.io.IOException;
import java.math.BigInteger;

import com.android.obex.ClientSession;
import com.android.obex.HeaderSet;
import com.android.obex.ResponseCodes;

/* Place a message into current directory on MSE. */
@VisibleForTesting
public class RequestPushMessage extends Request {

    private static final String TYPE = "x-bt/message";
    private Bmessage mMsg;
    private String mMsgHandle;

    private RequestPushMessage(String folder) {
        mHeaderSet.setHeader(HeaderSet.TYPE, TYPE);
        if (folder == null) {
            folder = "";
        }
        mHeaderSet.setHeader(HeaderSet.NAME, folder);
    }

    RequestPushMessage(String folder, Bmessage msg, CharsetType charset, boolean transparent,
            boolean retry) {
        this(folder);
        mMsg = msg;
        ObexAppParameters oap = new ObexAppParameters();
        oap.add(OAP_TAGID_TRANSPARENT, transparent ? TRANSPARENT_ON : TRANSPARENT_OFF);
        oap.add(OAP_TAGID_RETRY, retry ? RETRY_ON : RETRY_OFF);
        oap.add(OAP_TAGID_CHARSET, charset == CharsetType.NATIVE ? CHARSET_NATIVE : CHARSET_UTF8);
        oap.addToHeaderSet(mHeaderSet);
    }

    @Override
    protected void readResponseHeaders(HeaderSet headerset) {
        try {
            String handle = (String) headerset.getHeader(HeaderSet.NAME);
            if (handle != null) {
                /* just to validate */
                new BigInteger(handle, 16);

                mMsgHandle = handle;
            }
        } catch (NumberFormatException e) {
            mResponseCode = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        } catch (IOException e) {
            mResponseCode = ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
    }

    public Bmessage getBMsg() {
        return mMsg;
    }

    public String getMsgHandle() {
        return mMsgHandle;
    }

    @Override
    public void execute(ClientSession session) throws IOException {
        executePut(session, BmessageBuilder.createBmessage(mMsg).getBytes());
    }
}
