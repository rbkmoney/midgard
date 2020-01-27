package com.rbkmoney.midgard.endpoint;

import com.rbkmoney.damsel.schedule.ScheduledJobExecutorSrv;
import com.rbkmoney.woody.thrift.impl.http.THServiceBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;

@WebServlet("/v1/clearing_scheduler_job")
public class ClearingSchedulerEndpoint extends GenericServlet {

    private Servlet thriftServlet;

    @Autowired
    private ScheduledJobExecutorSrv.Iface requestHandler;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        thriftServlet = new THServiceBuilder().build(ScheduledJobExecutorSrv.Iface.class, requestHandler);
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        thriftServlet.service(request, response);
    }
}
