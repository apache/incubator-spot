def get_random_ip(cidr):
    import ipaddress
    import random
    net = ipaddress.IPv4Network(cidr)
    return net[ random.randint(0, net.num_addresses-1) ]

def get_email():
    import random
    fn = ['noah','emma','mason','ethan','james','madison','daniel','ray','camille','clark','bruce','diana','flash']
    ln = ['smith','gold','hunt','knight','fisher','cook','clark','kent','wayne','prince','gordon']
    dom = ['example.com','outlook.com','skype.com','hotmail.com','yahoo.com','gmail.com','secure.com','cnn.com','nbc.com','news.com']
    email = fn[random.randrange(0, len(fn))] + '.' + ln[random.randrange(0, len(ln))] + '@' + dom[random.randrange(0, len(dom))]
    return email

def get_rcpt(min_=1, max_=3):
    import random
    cnt = random.randint(min_, max_)
    rcpt = [ get_email() for x in range(0, cnt) ]
    return [ ','.join(rcpt), cnt ]
