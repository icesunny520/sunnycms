package com.jspxcms.common.web;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.sunny.database.DBConnectionManager;
import com.sunny.database.DataRow;
import com.sunny.database.DataTable;
import com.sunny.database.Database;

public class BusGeoServlet extends HttpServlet {

	private static Database db;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String t = req.getParameter("type");
		if (StringUtils.isEmpty(t)) {
			// 查询数据库车辆信息
			try {
				DataTable dt = db.executeQuery("SELECT bus_line.endStation, bus_line.id FROM bus_info INNER JOIN bus_line ON bus_info.line = bus_line.id group by bus_line.id");
				ArrayList<String[]> lines = new ArrayList<>();
				if (dt.getRowCount() > 0) {
					for (DataRow dr : dt.getRows()) {
						String[] v = new String[2];
						v[0] = dr.getString("endStation");
						v[1] = dr.getString("id");
						lines.add(v);
					}
				}
				req.setAttribute("lines", lines);
			} catch (SQLException e) {
				throw new java.lang.RuntimeException(e);
			}

			String ctx = req.getContextPath();
			req.getRequestDispatcher(ctx + "/geo/busLocation.jsp").forward(req, resp);
		} else if (t.equals("geo")) {
			String line = req.getParameter("line");
			try {
				DataTable dt = db.executeQuery("SELECT bus_geo.longitude, bus_geo.latitude, bus_geo.direction, bus_info.busNum FROM bus_geo INNER JOIN bus_info ON bus_geo.bus = bus_info.geoDevID WHERE bus_info.line=?", line.trim());
				ArrayList<GeoModel> geos = new ArrayList<>();
				if (dt.getRowCount() > 0) {
					for (DataRow dr : dt.getRows()) {
						GeoModel gm = new GeoModel();
						gm.setBusNum(dr.getString("busNum"));
						gm.setDirection(dr.getString("direction"));
						gm.setLatitude(dr.getString("latitude"));
						gm.setLongitude(dr.getString("longitude"));
						geos.add(gm);
					}
				}
				resp.getOutputStream().write(JSON.toJSONString(geos).toString().getBytes("UTF-8"));  
			    resp.setContentType("text/json; charset=UTF-8");  
			} catch (Exception e) {
				throw new java.lang.RuntimeException(e);
			}
		}

	}

	@Override
	public void init() throws ServletException {
		Properties prop = new Properties();
		InputStream is = null;
		try {
			if (db == null) {
				is = BusGeoServlet.class.getClassLoader().getResourceAsStream("cmsdb_config.xml");
				prop.loadFromXML(is);
				db = DBConnectionManager.getInstance().getDatabase(prop);
			}
		} catch (Exception e) {
			throw new java.lang.RuntimeException(e);
		}
		super.init();
	}

}
