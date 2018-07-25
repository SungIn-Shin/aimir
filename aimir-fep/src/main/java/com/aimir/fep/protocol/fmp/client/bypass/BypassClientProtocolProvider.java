package com.aimir.fep.protocol.fmp.client.bypass;

import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class BypassClientProtocolProvider {

    private static ProtocolCodecFactory CODEC_FACTORY = 
            new ProtocolCodecFactory()
        {
            public ProtocolDecoder getDecoder(IoSession session) throws Exception
            {
                return new BypassClientDecoder();
            }

            public ProtocolEncoder getEncoder(IoSession session) throws Exception
            {
                return new BypassClientEncoder();
            }
        };

        /**
         * inherited method from ProtocolProvider
         *
         * @return codefactory <code>ProtocolCodecFactory</code>
         */
        public ProtocolCodecFactory getCodecFactory()
        {
            return CODEC_FACTORY;
        }

        /**
         * inherited method from ProtocolProvider
         *
         * @return handler <code>ProtocolHandler</code>
         */
        public IoHandler getHandler()
        {
            return new BypassClientHandler();
        }
}
