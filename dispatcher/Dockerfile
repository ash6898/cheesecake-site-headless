FROM httpd:2.4

# Create cache directory for mod_cache_disk
RUN mkdir -p /var/cache/apache2/proxy && \
    chown www-data:www-data /var/cache/apache2/proxy

# Replace default httpd.conf with ours
COPY httpd.conf /usr/local/apache2/conf/httpd.conf

# Fix Windows line endings
RUN sed -i 's/\r//' /usr/local/apache2/conf/httpd.conf