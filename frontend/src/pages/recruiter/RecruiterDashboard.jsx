import React, { useEffect, useState } from 'react'
import api from '../../api/axios'
import Navbar from '../../components/Navbar'

export default function RecruiterDashboard() {
  const [drives, setDrives] = useState([])
  const [selectedDrive, setSelectedDrive] = useState(null)
  const [applicants, setApplicants] = useState([])
  const [error, setError] = useState('')

  const loadDrives = async () => {
    try {
      const { data } = await api.get('/recruiter/drives')
      setDrives(data)
    } catch (err) {
      setError('Failed to load drives')
    }
  }

  useEffect(() => { loadDrives() }, [])

  const viewApplicants = async (drive) => {
    setSelectedDrive(drive)
    setError('')
    try {
      const { data } = await api.get(`/recruiter/drives/${drive.id}/applicants`)
      setApplicants(data)
    } catch (err) {
      setError('Failed to load applicants')
    }
  }

  const updateStatus = async (applicationId, status) => {
    try {
      await api.patch(`/recruiter/applications/${applicationId}/status`, { status })
      viewApplicants(selectedDrive)
    } catch (err) {
      setError('Failed to update status')
    }
  }

  return (
    <div>
      <Navbar />
      <div className="container">
        <h1>Recruiter Dashboard</h1>
        {error && <div className="error-banner">{error}</div>}

        <div className="card">
          <h3>My Drives</h3>
          {drives.length === 0 ? (
            <p className="muted">No drives are linked to your account yet. Ask your TPO to attach a drive to your recruiter profile.</p>
          ) : (
            <table>
              <thead><tr><th>Title</th><th>Status</th><th>Date</th><th></th></tr></thead>
              <tbody>
                {drives.map((d) => (
                  <tr key={d.id}>
                    <td>{d.title}</td>
                    <td><span className={`badge ${d.status.toLowerCase()}`}>{d.status}</span></td>
                    <td>{d.driveDate}</td>
                    <td><button className="secondary" onClick={() => viewApplicants(d)}>View Applicants</button></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

        {selectedDrive && (
          <div className="card">
            <h3>Applicants for {selectedDrive.title}</h3>
            {applicants.length === 0 ? (
              <p className="muted">No applicants yet.</p>
            ) : (
              <table>
                <thead><tr><th>Name</th><th>Branch</th><th>%</th><th>Backlogs</th><th>Resume</th><th>Status</th><th></th></tr></thead>
                <tbody>
                  {applicants.map((a) => (
                    <tr key={a.id}>
                      <td>{a.studentName}</td>
                      <td>{a.branch}</td>
                      <td>{a.percentage}</td>
                      <td>{a.backlogs}</td>
                      <td>{a.resumeUrl ? <a href={a.resumeUrl} target="_blank" rel="noreferrer">View</a> : '—'}</td>
                      <td><span className={`badge ${a.status.toLowerCase()}`}>{a.status}</span></td>
                      <td>
                        <button className="secondary" onClick={() => updateStatus(a.id, 'SHORTLISTED')}>Shortlist</button>{' '}
                        <button className="danger" onClick={() => updateStatus(a.id, 'REJECTED')}>Reject</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        )}
      </div>
    </div>
  )
}
