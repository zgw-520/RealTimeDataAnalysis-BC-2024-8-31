#!/usr/bin/env python
# -*- coding: utf-8 -*-

import random
import time

def get_one_day_str():
    a1=(2019,1,1,0,0,0,0,0,0)              #设置开始日期时间元组（2019-01-01 00：00：00）
    a2=(2019,12,31,23,59,59,0,0,0)    #设置结束日期时间元组（2019-12-31 23：59：59）

    start=time.mktime(a1)    #生成开始时间戳
    end=time.mktime(a2)      #生成结束时间戳

    t=random.randint(start,end)    #在开始和结束时间戳中随机取出一个
    date_touple=time.localtime(t)          #将时间戳生成时间元组
    date=time.strftime("%Y-%m-%d",date_touple)  #将时间元组转成格式化字符串
    return date

def sample_oneday_log(date_str):
    for H in range(0, 24):
        H_str = str(H)
        if H < 10:	H_str = '0'+str(H)

        for m in range(0, 60):
            m_str = str(m)
            if m < 10: m_str = '0'+str(m)
            time_s = '{}:{}:00'.format(H_str, m_str)
            time_str = '{} {}'.format(date_str, time_s)
            id = random.sample(['2400', '7361', '7363'], 1)[0]
            ns = []

            if id == '2400':
                ns. append(str(random.randint(0, 18)))
                ns. append(str(random.randint(0, 5)))
                ns. append(str(random.randint(30, 50)) + '.' + str(random.randint(0, 9)))
            elif id == '7361':
                ns. append(str(random.randint(0, 24)))
                ns. append(str(random.randint(0, 7)))
                ns. append(str(random.randint(40, 60)) + '.' + str(random.randint(0, 9)))
            elif id == '7363':
                ns. append(str(random.randint(0, 14)))
                ns. append(str(random.randint(0, 3)))
                ns. append(str(random.randint(35, 50)) + '.' + str(random.randint(0, 9)))

            for i in range(3, 16):
                n = str(random.randint(0, 10)) + '.' + str(random.randint(0, 9))
                ns.append(n)

            log = '{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{},{}'.format(id, '22', 'vm-914', 
                ns[0], ns[1], ns[2], ns[3], ns[4], ns[5], ns[6], ns[7], ns[8], ns[9], 
                ns[10], ns[11], ns[12], ns[13], ns[14], ns[15], time_str)

            print(log)

if __name__ == "__main__":
    sample_oneday_log(get_one_day_str())
