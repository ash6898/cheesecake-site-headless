FROM httpd:2.4

COPY httpd.conf /usr/local/apache2/conf/httpd.conf
COPY cgi-bin/invalidate /usr/local/apache2/cgi-bin/invalidate
COPY docker-entrypoint.sh /usr/local/bin/cheesecake-entrypoint.sh

RUN sed -i 's/\r//' /usr/local/apache2/conf/httpd.conf \
        /usr/local/apache2/cgi-bin/invalidate \
        /usr/local/bin/cheesecake-entrypoint.sh \
    && chmod 755 /usr/local/apache2/cgi-bin/invalidate \
        /usr/local/bin/cheesecake-entrypoint.sh

ENTRYPOINT ["/usr/local/bin/cheesecake-entrypoint.sh"]
CMD ["httpd-foreground"]
