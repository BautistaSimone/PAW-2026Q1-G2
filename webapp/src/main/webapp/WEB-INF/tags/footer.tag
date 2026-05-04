<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>

<footer class="footer-bbdiscos">
    <div class="footer-container">
        <div class="footer-content">
            <div class="footer-section-contact">
                <h4 class="footer-title"><spring:message code="Global.brand"/></h4>
                <div class="footer-contact">
                    <a href="mailto:<spring:message code="Global.email"/>" class="footer-link">
                        <i class="bi bi-envelope" aria-hidden="true"></i>
                        <spring:message code="Global.email"/>
                    </a>
                    <a href="#" class="footer-link">
                        <i class="bi bi-geo-alt" aria-hidden="true"></i>
                        <spring:message code="Global.location"/>
                    </a>
                </div>
            </div>
        </div>
        <div class="footer-bottom">
            <p class="footer-copyright">
                <spring:message code="Footer.copyright" />
            </p>
            <p class="footer-legal">
                <spring:message code="Footer.consumerDefense" />
                <a
                        href="https://autogestion.produccion.gob.ar/consumidores"
                        target="_blank"
                        rel="noopener noreferrer"
                >
                    <spring:message code="Footer.enterHere" />
                </a>
                . / <a href="#"><spring:message code="Footer.regretButton" /></a>
            </p>
        </div>
    </div>
</footer>
