package com.rbkmoney.midgard.base.clearing.endpoint;

import com.rbkmoney.midgard.ClearingServiceSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

/** Класс, инкапсулирующий логику создания сервера для взаимодейстаия с клиринговым сервисом */
@WebServlet("/v1/clearing_service")
public class ClearingServiceEndpoint extends GenericServlet {

    private Servlet thriftServlet;

    @Autowired
    private ClearingServiceSrv.Iface clearingServiceHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(ClearingServiceSrv.Iface.class, clearingServiceHandler);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
