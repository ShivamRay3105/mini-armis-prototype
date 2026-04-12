import { Link } from "react-router-dom";

const LinkPage = () => {
    return (
        <section>
            <h1>SentinelStream Access</h1>
            <br />
            <h2>Public</h2>
            <Link to="/login">Login</Link>
            <br />
            <h2>Private Dashboards</h2>
            <Link to="/security_analyst">Security Analyst Console</Link>
            <br />
            <a href="http://localhost:9000">System Admin (Spring Boot Admin)</a>
        </section>
    );
};

export default LinkPage;