package com.rbkmoney.midgard.adapter.mts.endpoints;

import com.rbkmoney.midgard.ClearingAdapterSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/v1/mts_clearing_adapter")
public class ClearingAdapterEndpoint extends GenericServlet {

    private Servlet thriftServlet;

    @Autowired
    private ClearingAdapterSrv.Iface clearingAdapterService;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder()
                .build(ClearingAdapterSrv.Iface.class, clearingAdapterService);
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        thriftServlet.service(req, res);
    }
}
