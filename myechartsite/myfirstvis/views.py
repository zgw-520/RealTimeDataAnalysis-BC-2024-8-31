# coding:utf-8
from __future__ import unicode_literals
import math

from django.http import HttpResponse
from django.template import loader
from pyecharts import Line3D
from pyecharts import Bar
from pyecharts import Page
from pyecharts import Liquid
from pyecharts import Pie
from pyecharts import Radar

import redis
pool = redis.ConnectionPool(host='127.0.0.1', port=6379, password="123456")
r = redis.Redis(connection_pool=pool)


REMOTE_HOST = "https://pyecharts.github.io/assets/js"

def index(request):
    template = loader.get_template('myfirstvis/pyecharts.html')
    
    page = Page()
    page.add(DeviceInvalidCountBar())
    page.add(DeviceTotalPie())
    page.add(DeviceProcessRadar())
    page.add(DeviceTempBar())
    context = dict(
        myechart = page.render_embed(),
        host = REMOTE_HOST,
        script_list = page.get_js_dependencies()
    )
    return HttpResponse(template.render(context, request))

def DeviceInvalidCountBar():
    attr = ["DEVICE_2400_INVALID", "DEVICE_7361_INVALID", "DEVICE_7363_INVALID"]
    values = r.mget(attr)
    for i, x in enumerate(values):
        values[i] = int(x.decode().split('.')[0])
    bar = Bar("各设备生产异常总数", "各设备生产物品异常总数")
    bar.add("", attr, values, mark_line=["average"], mark_point=["max", "min"])
    return bar

def DeviceTotalPie():
    attr = ["DEVICE_2400", "DEVICE_7361", "DEVICE_7363"]
    values = r.mget(attr)
    values = [int(x.decode().split('.')[0]) for x in values]
    pie = Pie("各设备生产物品比例", '')
    pie.add('设备id', attr, values, is_label_show=True)
    return pie

def DeviceProcessRadar():
    attr = ["DEVICE_2400", "DEVICE_7361", "DEVICE_7363"]
    vs = r.mget(attr)
    vs = [int(x.decode().split('.')[0]) for x in vs]
    schema = [('DEVICE_2400', vs[0]), ('DEVICE_7361', vs[1]), ('DEVICE_7363', vs[2])]
    
    attr2 = ["DEVICE_2400_INVALID", "DEVICE_7361_INVALID", "DEVICE_7363_INVALID"]
    values = r.mget(attr)
    values = [int(x.decode().split('.')[0]) for x in values]
    v1 = [values]
    
    radar = Radar()
    radar.config(schema)
    radar.add("设备生产雷达图", v1, is_splitline=True, is_axisline_show=True)
    return radar

    
def DeviceTempBar():
    attr0 = ["DEVICE_2400_C", "DEVICE_7361_C", "DEVICE_7363_C"]
    vs = r.mget(attr0)
    vs = [int(x.decode()) for x in vs]

    attr = ["DEVICE_2400_T", "DEVICE_7361_T", "DEVICE_7363_T"]
    values = r.mget(attr)
    for i, x in enumerate(values):
        values[i] = int(x.decode().split(".")[0]) / vs[i]
    bar = Bar("各设备运行时温度的平均值", "")
    bar.add("", attr, values, mark_line=["average"], mark_point=["max", "min"])
    return bar